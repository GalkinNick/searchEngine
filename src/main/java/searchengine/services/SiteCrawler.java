package searchengine.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.IndexEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SiteCrawler extends RecursiveTask<Set<String>> {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";

    private final String url;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private final AtomicBoolean isRootTask;
    private final SiteParser siteParser;

    @Getter
    private final Set<String> resltSet = new LinkedHashSet<>();

    @Autowired
    public SiteCrawler(String url,
                       SiteRepository siteRepository,
                       PageRepository pageRepository,
                       LemmaRepository lemmaRepository,
                       IndexRepository indexRepository,
                       AtomicBoolean isRootTask) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.isRootTask = isRootTask;
        siteParser = new SiteParser(url);
    }


    protected Set<String> connect(){
        Set<String> set = new LinkedHashSet<>();
        try{
            Document doc = Jsoup.connect(url).timeout(0)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .get();

            Elements links = doc.select("a[href]");

            for(Element element : links){
                String link = element.absUrl("href").replaceAll("/$", "");
                if(!resltSet.add(link) && !link.endsWith(".png")
                        || !link.endsWith(".jpg") && !link.endsWith("#") && !link.startsWith("tel:")) {

                    if (!link.trim().equals(url)){
                        resltSet.add(link);
                    }
                    if (link.trim().endsWith(".pdf")) {
                        resltSet.remove(link);
                    }
                    if (link.endsWith("#")){
                        resltSet.remove(link);
                    }
                    if (!link.startsWith("http")){
                        resltSet.remove(link);
                    }
                    if (!link.contains(url)){
                        resltSet.remove(link);
                    }

                    set.add(link);
                }
            }
        } catch (IOException ex){
            log.warn("Failed to connect - {}", ex.getMessage());
        }
        return set;
    }

    private List<SiteCrawler> createSubtasks(Set<String> links){
        return  links.stream().
                map(link -> new SiteCrawler(link, siteRepository, pageRepository, lemmaRepository, indexRepository, isRootTask))
                .toList();
    }

    @Override
    protected Set<String> compute() {

        if (isRootTask.get()) {
            try {
                Integer siteId = siteRepository.findIdByUrl(url);
                if (siteId != null) {
                    SiteEntity siteEntity = siteRepository.findById(siteId).get();

                    PageEntity pageEntity = createAndSavePageEntity(siteEntity, siteParser.getHtmlContentFromPage(url), getResponseCode(url));

                    createLemmaEntity(siteEntity, pageEntity);
                }

                List<SiteCrawler> subtasks = createSubtasks(connect());

                for (SiteCrawler site : subtasks) {
                    site.fork();
                }

                for (SiteCrawler site : subtasks) {
                    site.join();
                }

            } catch (IOException ex) {
                log.warn("Failed to create lemma - {}", ex.getMessage());
            }
        }
        return resltSet;
    }


    private PageEntity createAndSavePageEntity(SiteEntity siteEntity, String htmlContent, int code){

        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(url);
        pageEntity.setSiteEntityId(siteEntity);
        pageEntity.setContent(htmlContent);
        pageEntity.setCode(code);
        pageRepository.save(pageEntity);

        return pageEntity;
    }


      private void createLemmaEntity(SiteEntity siteEntity, PageEntity pageEntity) throws IOException {

        WordToLemmaConverter converting = new WordToLemmaConverter();

        String text = converting.removeHtmlTag(siteParser.getHtmlContentFromPage(url));
        HashMap<String, Integer> lemmasMap = converting.convertingIntoLemmas(text);

        try {
            lemmasMap.keySet().forEach(lemma -> {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSite(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(lemmasMap.get(lemma));
                lemmaRepository.save(lemmaEntity);

                try {
                    IndexEntity indexEntity = new IndexEntity();
                    indexEntity.setPagesId(pageEntity.getId());
                    indexEntity.setLemmaId(lemmaEntity);
                    indexEntity.setRank((float) lemmasMap.get(lemma));

                    indexRepository.save(indexEntity);
                }
                catch (NullPointerException ex){
                    log.warn("Failed to save index- {}", ex.getMessage());
                }
            });
        }
        catch (NullPointerException ex){
            log.warn("Failed to save lemma - {}", ex.getMessage());
        }
    }

    private Integer getResponseCode(String urlPage) throws IOException {
        URL url = new URL(urlPage);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getResponseCode();
    }

}

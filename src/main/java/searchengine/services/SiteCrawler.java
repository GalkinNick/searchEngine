package searchengine.services;

import lombok.Getter;
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
import java.net.SocketException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

//@Service
public class SiteCrawler extends RecursiveTask<Set<String>> {

    //@Value("${app.connection.userAgent}")
    private String userAgent = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";

    //@Value("${app.connection.referrer}")
    private String referrer = "http://www.google.com";

    private final String url;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private final AtomicBoolean isRootTask;


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
    }


    protected Set<String> connect(){
        Set<String> set = new LinkedHashSet<>();
        try{
            Document doc = Jsoup.connect(url).timeout(0)//(int)(Math.random() * 1000))//40*10000)
                    .userAgent(userAgent)
                    .referrer(referrer)
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
        } catch (Exception ex){
            ex.printStackTrace();
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

                    PageEntity pageEntity = createAndSavePageEntity(siteEntity, getHtmlContentFromPage(url), getResponseCode(url));

                    createLemmaEntity(siteEntity, pageEntity);
                }

                List<SiteCrawler> subtasks = createSubtasks(connect());

                for (SiteCrawler site : subtasks) {
                    site.fork();
                }

                for (SiteCrawler site : subtasks) {
                    site.join();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
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

        ConvertingWordsIntoLemmas converting = new ConvertingWordsIntoLemmas();

        String text = converting.removeHtmlTag(getHtmlContentFromPage(url));
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
                catch (Exception ex){
                    ex.printStackTrace();
                    System.out.println("Can`t save index");
                }
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Can`t save lemma");
        }
    }

    private String getHtmlContentFromPage(String url){
        StringBuilder builder = new StringBuilder();
        try {
            Document doc = Jsoup.connect(url).timeout(40 * 10000)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();

            Elements links = doc.select("a");
            for (Element link : links ){
                builder.append(link);
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return builder.toString();
    }


    private Integer getResponseCode(String urlPage) throws IOException {
        URL url = new URL(urlPage);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getResponseCode();
    }

}

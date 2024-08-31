package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Statuses;

import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private SitesList sites;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;

    private ForkJoinPool forkJoinPool = new ForkJoinPool();

    private  boolean isIndexing;

    @Override
    public IndexingResponse startIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing){
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else {
            response.setResult(true);
        }

        isIndexing = true;

        try {
            for (Site site : sites.getSites()) {

                SiteEntity siteEntity = new SiteEntity();

                deleteSiteByUrl(site.getUrl());

                createSiteEntity(siteEntity, site, response);

                SiteCrawler siteCrawler = new SiteCrawler(site.getUrl(), pageRepository, siteRepository);

                forkJoinPool.submit(siteCrawler);

                ConvertingWordsIntoLemmas converting
                        = new ConvertingWordsIntoLemmas();

                siteCrawler.compute()
                     .forEach(s -> {
                                 try {
                                     createPageEntity(s,
                                             siteEntity,
                                             siteCrawler.getHtmlContentFromPage(s),
                                             siteCrawler.getResponseCode(s));


                                     createLemmaEntity(converting,
                                             siteCrawler,
                                             siteEntity,
                                             s);


                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                             }
                );


                if (forkJoinPool.isQuiescent()){
                    siteEntity.setStatus(Statuses.INDEXED);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.save(siteEntity);
                }
                else if (!forkJoinPool.isTerminated()){
                    siteEntity.setStatus(Statuses.FAILED);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.save(siteEntity);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing){
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }

        isIndexing = false;

        forkJoinPool.shutdownNow();

        List<SiteEntity> indexingSites = siteRepository.findByStatus((Statuses.INDEXED).toString());
        for (SiteEntity indexingSite : indexingSites){
            indexingSite.setStatus(Statuses.FAILED);
            indexingSite.setLastError("Индексация остановлена пользователем");
            siteRepository.save(indexingSite);
        }
        return response;
    }


    @Override
    public IndexingResponse indexingPage(String url) {
        IndexingResponse response = new IndexingResponse();
        if (true) {
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле"
            );
        }

        Site site = new Site();
        site.setUrl(url);
        site.setName("Test");
        sites.getSites().add(site);

        return response;
    }


    private void deletePageById(Long id){
        pageRepository.deleteBySiteId(id);
    }

    private void deleteSiteByUrl(String url){
        try{
            Long siteId = siteRepository.findByUrl(url);
            if (siteId != null) {
                SiteEntity siteEntity = siteRepository.findById(siteId).get();
                siteRepository.delete(siteEntity);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Site not found");
        }
    }

    private void createSiteEntity(SiteEntity siteEntity, Site site, IndexingResponse response){
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteEntity.setStatus(Statuses.INDEXING);
        siteEntity.setLastError(response.getError());
        siteEntity.setStatusTime(LocalDateTime.now());

        siteRepository.save(siteEntity);
    }


    private void createPageEntity(String path, SiteEntity siteEntity, String htmlContent, int code){
        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(path);
        pageEntity.setSiteEntityId(siteEntity);
        pageEntity.setContent(htmlContent);
        pageEntity.setCode(code);
        pageRepository.save(pageEntity);
    }

    private void createLemmaEntity(ConvertingWordsIntoLemmas converting,
                                   SiteCrawler siteCrawler,
                                   SiteEntity siteEntity,
                                   String s) throws IOException {

        String text = converting.removeHtmlTag(siteCrawler.getHtmlContentFromPage(s));
        HashMap<String, Integer> lemmasMap = converting.convertingIntoLemmas(text);

        try {
            lemmasMap.keySet().forEach(lemma -> {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSite(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(1);
                lemmaRepository.save(lemmaEntity);
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Don`t work");
        }
    }
}
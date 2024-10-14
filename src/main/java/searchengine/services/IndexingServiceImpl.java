package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.IndexingResponse;
import searchengine.model.*;

import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
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
    @Autowired
    private IndexRepository indexRepository;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private static final AtomicBoolean stopFlag = new AtomicBoolean(false);

    private  boolean isIndexing;

    @Override
    public synchronized IndexingResponse startIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing){
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else {
            response.setResult(true);
        }

        isIndexing = true;
        stopFlag.set(true);

        try {
            for (Site site : sites.getSites()) {

                deleteSiteByUrl(site.getUrl());

                SiteEntity siteEntity = createSiteEntity(site, response);

                forkJoinPool.execute(new SiteCrawler(site.getUrl(),
                        siteRepository,
                        pageRepository,
                        lemmaRepository,
                        indexRepository,
                        stopFlag));


                if (forkJoinPool.isQuiescent()){
                    siteEntity.setStatus(Statuses.INDEXED);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.save(siteEntity);
                }
                else if (!forkJoinPool.isTerminated()){
                    siteEntity.setStatus(Statuses.INDEXING);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.save(siteEntity);
                }
            }

        } catch (NullPointerException ex){
            log.warn("Failed to index the site - {}", ex.getMessage());
        }
        return response;
    }


    @Override
    public synchronized IndexingResponse stopIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing){
            response.setResult(true);


        } else {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }

        isIndexing = false;

        stopFlag.set(false);

        List<SiteEntity> indexingSites = siteRepository.findByStatus((Statuses.INDEXING));

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

        SiteParser siteParser = new SiteParser(url);

        Site site = new Site();
        site.setUrl(url);
        site.setName(siteParser.getSiteName());
        sites.getSites().add(site);

        return response;
    }

    private void deletePageById(Integer id){
        pageRepository.deleteBySiteId(id);
    }

    private void deleteSiteByUrl(String url){
        try{
            Integer siteId = siteRepository.findIdByUrl(url);
            if (siteId != null) {
                SiteEntity siteEntity = siteRepository.findById(siteId).get();
                siteRepository.delete(siteEntity);
            }
        }
        catch (NullPointerException ex){
            log.warn("Site not found - {}", ex.getMessage());
        }
    }

    private SiteEntity createSiteEntity(Site site, IndexingResponse response){
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteEntity.setStatus(Statuses.INDEXING);
        siteEntity.setLastError(response.getError());
        siteEntity.setStatusTime(LocalDateTime.now());

        siteRepository.save(siteEntity);

        return  siteEntity;
    }

}
package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Statuses;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    private ForkJoinPool forkJoinPool = new ForkJoinPool();

    private JdbcTemplate jdbcTemplate;

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

        List<Site> sitesList = sites.getSites();
        sites.getSites().clear();

        try {
            for (Site site : sitesList) {

                SiteEntity siteEntity = new SiteEntity();


                siteRepository.deleteSitesById(siteEntity.getId());

                siteEntity.setUrl(site.getUrl());
                siteEntity.setName(site.getName());
                siteEntity.setStatus(Statuses.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());

                siteRepository.saveSite(siteEntity);

                SiteCrawler siteCrawler = new SiteCrawler(site.getUrl());

                Set<String> invoke = forkJoinPool.invoke(siteCrawler);

                for (String pageLink : invoke){
                    try {
                        Connection connection = Jsoup.connect(pageLink);
                      //  pageList.add();
                        Thread.sleep(150);

                    } catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                }

               // pageRepository.savePages(pageList);

                if (forkJoinPool.isQuiescent()){
                    siteEntity.setStatus(Statuses.INDEXED);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.saveSite(siteEntity);
                }
                else if (!forkJoinPool.isTerminated()){
                    siteEntity.setStatus(Statuses.FAILED);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.saveSite(siteEntity);
                }
            }
        } catch (Exception e){
            e.printStackTrace();


        }
        return response;
    }
}

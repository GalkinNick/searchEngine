package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.IndexingResponse;
import searchengine.dto.response.SearchResponse;
import searchengine.dto.response.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    private final SearchService searchService;


    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService)
    {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing(){
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    @ResponseBody
    public ResponseEntity<IndexingResponse> postPage(@RequestParam String url){
        return ResponseEntity.ok(indexingService.indexingPage(url));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<SearchResponse> search(@RequestParam String query,
                                                 @RequestParam(required = false, defaultValue = "0") Integer offset,
                                                 @RequestParam(required = false, defaultValue = "20")  Integer limit,
                                                 @RequestParam(required = false) String site) throws IOException {

        /*searchService.search(query, offset, limit, site).getData().forEach(l -> {
            System.out.println(l.getSite() + l.getUri());
        });*/
        return ResponseEntity.ok(searchService.search(query, offset, limit, site));
    }


}



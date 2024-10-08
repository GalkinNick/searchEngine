package searchengine.services;

import searchengine.dto.response.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexingPage(String url);

}

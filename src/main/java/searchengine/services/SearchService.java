package searchengine.services;

import org.springframework.web.bind.annotation.RequestParam;
import searchengine.dto.response.SearchResponse;

import java.io.IOException;

public interface SearchService {
    SearchResponse search(String query, Integer offset, Integer limit, String site) throws IOException;
}

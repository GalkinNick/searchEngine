package searchengine.dto.response;

import lombok.Data;
import searchengine.dto.search.DetailedSearchItem;

import java.util.List;

@Data
public class SearchResponse {
    private boolean result;
    private int count;
    private List<DetailedSearchItem> data;
    private String error;
}

package searchengine.dto.search;

import lombok.Data;
import searchengine.model.PageEntity;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchPages {
    private List<PageEntity> pageEntityList = new ArrayList<>();
}

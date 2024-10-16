package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSOutput;
import searchengine.dto.response.SearchResponse;
import searchengine.dto.search.DetailedSearchItem;
import searchengine.dto.search.PageFoundState;
import searchengine.dto.search.SearchPages;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String EMPTY_QUERY_MESSAGE = "Задан пустой поисковый запрос";
    private static final String PAGE_NOT_FOUND_MESSAGE = "Указанная страница не найдена";

    private final PageFoundState pageFoundState = new PageFoundState();
    private final SearchPages searchPages = new SearchPages();

    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private PageRepository pageRepository;

    @Override
    public SearchResponse search(String query, Integer offset, Integer limit, String site) throws IOException {

        WordToLemmaConverter converting = new WordToLemmaConverter();

        //Исключать из полученного списка леммы, которые встречаются на слишком большом количестве страниц.
        HashMap<String, Integer> lemmas = eliminateFrequentLemma(converting.convertingIntoLemmas(query));

        //сортировать леммы
        HashMap<String, Integer> sortedLemmas = sortLemmasInAscendingOrder(lemmas);

        //По первой, самой редкой лемме из списка, находить все страницы, на которых она встречается.
        String firstLemma = findPagesByFirstLemma(sortedLemmas.keySet(), site);
        sortedLemmas.remove(firstLemma);

        //поиск страниц по оставшимся леммам из списка
        HashMap<PageEntity, List<LemmaEntity>> readyPages = findPagesByLemma(sortedLemmas.keySet());

        HashMap<PageEntity, Float> absoluteRelevance = considerAbsoluteRelevance(readyPages);

        List<DetailedSearchItem> details = new ArrayList<>();

        for (Map.Entry<PageEntity, List<LemmaEntity>> page : readyPages.entrySet()){
            DetailedSearchItem detailedSearchItem = new DetailedSearchItem();
            SiteParser siteParser = new SiteParser(page.getKey().getPath());

            detailedSearchItem.setSite(page.getKey().getSiteEntityId().getUrl());
            detailedSearchItem.setSiteName(page.getKey().getSiteEntityId().getName());
            detailedSearchItem.setUri(replaceUrl(page.getKey().getSiteEntityId().getUrl(), page.getKey().getPath()));;
            detailedSearchItem.setTitle(siteParser.getPageTitle());
            detailedSearchItem.setSnippet(siteParser.getSnippet(page.getValue()));
            detailedSearchItem.setRelevance(absoluteRelevance.get(page.getKey()));

            details.add(detailedSearchItem);
        }

        SearchResponse searchResponse = new SearchResponse();

        if (!query.isEmpty() && !pageFoundState.isPageNotFound()) {
            searchResponse.setResult(true);
            searchResponse.setCount(readyPages.size());
            searchResponse.setData(details);
        } else if (pageFoundState.isPageNotFound()){
            searchResponse.setResult(false);
            searchResponse.setError(PAGE_NOT_FOUND_MESSAGE);
        }
        else{
            searchResponse.setResult(false);
            searchResponse.setError(EMPTY_QUERY_MESSAGE);
        }

        return searchResponse;
    }


    private HashMap<String, Integer> sortLemmasInAscendingOrder(HashMap<String, Integer> lemma){

        return lemma.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String findPagesByFirstLemma(Set<String> lemmasSet, String site){
        String lemma = null;
        List<PageEntity> pageEntityList = new ArrayList<>();

        if (!lemmasSet.isEmpty()) {
            lemma = lemmasSet.iterator().next();
        } else {
            pageFoundState.setPageNotFound(true);
        }

        try {
            lemmaRepository.getListLemmas(lemma).forEach(l -> {
                if (site != null) {
                    if (l.getSite().getUrl().equals(site)) {
                       pageEntityList.add(getPageByLemma(l));
                    }
                    else {
                        pageFoundState.setPageNotFound(true);
                    }
                }
                else {
                    pageEntityList.add(getPageByLemma(l));
                }
            });
        }
        catch (NullPointerException ex){
            pageFoundState.setPageNotFound(true);
            log.warn("Failed to find the lemma - {}", ex.getMessage());
        }

        searchPages.setPageEntityList(pageEntityList);
        return lemma;
    }

    private PageEntity getPageByLemma(LemmaEntity lemmaEntity){

        PageEntity pageEntity =  new PageEntity();
        try {
            IndexEntity indexEntity = indexRepository.getIndexEntity(lemmaEntity);
            int id = indexEntity.getPagesId();
            pageEntity = pageRepository.findById(id).get();

        }
        catch (NullPointerException ex){
            log.warn("Failed to get page - {}", ex.getMessage());
        }
        return pageEntity;
    }

    private HashMap<String, Integer> eliminateFrequentLemma(HashMap<String, Integer> lemmaMap){

        HashMap<String, Integer> allLemma = new HashMap<>();

        final int MIN_VALUE = 0;
        int pageCount = 0;


        for (Map.Entry<String, Integer> lemma : lemmaMap.entrySet()) {

            List<LemmaEntity> listLemma = lemmaRepository.getListLemmas(lemma.getKey());

            if (listLemma != null) {
                allLemma.put(lemma.getKey(), listLemma.size());
            } else {
                pageFoundState.setPageNotFound(true);
            }
        }

        for (Map.Entry<String, Integer> lemma : allLemma.entrySet()){
            pageCount += lemma.getValue();
        }

        Iterator<Map.Entry<String, Integer>> iterator = allLemma.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue() > (pageCount / allLemma.size()) || entry.getValue() == MIN_VALUE){
                    iterator.remove();
            }
        }
        return allLemma;
    }

    private HashMap<PageEntity, List<LemmaEntity>> findPagesByLemma(Set<String> lemmas){
        final int MIN_LEMMAS = 1;
        HashMap<PageEntity, List<LemmaEntity>> pagesMap = new HashMap<>();
        List<LemmaEntity> lemmaEntities = new ArrayList<>();

        if (lemmas.size() > MIN_LEMMAS) {
           for (String lemma : lemmas){
               for (int i = 0; i < searchPages.getPageEntityList().size(); i++) {
                   int finalIndex = i;
                    lemmaRepository.getListLemmas(lemma).forEach(l -> {
                       PageEntity pageEntity = getPageByLemma(l);
                       PageEntity searchPage = searchPages.getPageEntityList().get(finalIndex);
                       if (pageEntity.getPath().equals(searchPage.getPath())){
                           lemmaEntities.add(l);
                           pagesMap.put(searchPage, lemmaEntities);
                       }
                   });
               }
           }
        } else {
            System.out.println("Not lemmas");
        }
        return pagesMap;
    }


    private HashMap <PageEntity, Float> considerAbsoluteRelevance(HashMap<PageEntity, List<LemmaEntity>> readyPages){

        HashMap <PageEntity, Float> absoluteRelevance = new HashMap<>();

        final float START_RELEVANCE = 0;

        float relevance = START_RELEVANCE;

        for (Map.Entry<PageEntity, List<LemmaEntity>> pages : readyPages.entrySet()) {
            for (LemmaEntity lemmaEntity : pages.getValue()) {
                float rank = indexRepository.getRank(lemmaEntity);
                relevance += rank;
            }
            absoluteRelevance.put(pages.getKey(), relevance);
            relevance = START_RELEVANCE;
        }
        return absoluteRelevance;
    }

    private String replaceUrl(String url, String page){
        return page.replaceAll(url, "");
    }

}

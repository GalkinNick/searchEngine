package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import searchengine.model.LemmaEntity;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SiteParser {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";

    private final String url;

    public SiteParser(String url){
        this.url = url;
    }

    public String getHtmlContentFromPage(String url){
        StringBuilder builder = new StringBuilder();
        try {
            Document doc = Jsoup.connect(url).timeout(40 * 10000)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .get();

            Elements links = doc.select("a");
            for (Element link : links ){
                builder.append(link);
            }

        }
        catch (IOException ex){
            log.warn("Failed to get HTML content - {}", ex.getMessage());
        }
        return builder.toString();
    }


    public String getPageTitle(){
        String title = null;
        try{
            Document doc = Jsoup.connect(url).get();
            title = doc.title();
        }
        catch (IOException ex){
            log.warn("Failed to get page title - {}", ex.getMessage());
        }
        return  title;
    }

    public String getSiteName(){
        String siteName = "null";
        if (url.startsWith("http://")){
            url.replace("/", "");
            return   url.replace("http:www.", "");
        }
        else if (url.startsWith("https://")){
            if(url.endsWith("/")) {
                siteName = url.replace("/", "");
            }
            return  siteName.replace("https:www.", "");
        }
        return siteName;
    }

    public String getSnippet(List<LemmaEntity> lemmasEntity){

        StringBuilder builder = new StringBuilder();

        final int START_INDEX = -3;
        final int END_INDEX = 3;
        int index;

        String content = Jsoup.clean(getHtmlContentFromPage(url), Safelist.none());
        String[] words = content.split(" ");

        for (LemmaEntity lemmaEntity : lemmasEntity) {
            for (int i = 0; i < words.length; i++) {
                if (words[i].contains(lemmaEntity.getLemma())) {
                    index = START_INDEX;
                    while (index <= END_INDEX) {
                        if (index == 0){
                            builder.append("<b>" + words[i + index] + "</b>" + " ");
                            index++;
                        } else {
                            builder.append(words[i + index] + " ");
                            index++;
                        }
                    }
                }
            }
        }

        return String.valueOf(builder);
    }
}

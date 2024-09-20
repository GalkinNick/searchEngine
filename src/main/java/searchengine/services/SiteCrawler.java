package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.LemmaEntity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.RecursiveTask;

//@Service
public class SiteCrawler extends RecursiveTask<Set<String>> {

    //@Value("${app.connection.userAgent}")
    private String userAgent = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";

    //@Value("${app.connection.referrer}")
    private String referrer = "http://www.google.com";

    private final String url;

    private final Set<String> resltSet = new LinkedHashSet<>();

    @Autowired
    public SiteCrawler(String url) {
        this.url = url;
    }




    protected Set<String> connect(){
        Set<String> set = new LinkedHashSet<>();
        try{
            Document doc = Jsoup.connect(url).timeout(40*10000)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();

            Elements links = doc.select("a[href]");

            for(Element element : links){
                String link = element.absUrl("href").replaceAll("/$", "");
                if(!resltSet.add(link) || !link.endsWith(".png")
                        || !link.endsWith(".jpg") || !link.endsWith("#")) {
                    set.add(link);
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return set;
    }


    @Override
    protected Set<String> compute() {
        try {
            for (String link : connect()){

                if (!link.trim().equals(url)){
                    resltSet.add(link);
                }
                if (link.trim().endsWith(".pdf")) {
                    resltSet.remove(link);
                }
                if (link.endsWith("#")){
                    resltSet.remove(link);
                }
                if (!link.startsWith("http")){
                    resltSet.remove(link);
                }
                if (!link.contains(url)){
                    resltSet.remove(link);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return resltSet;
    }

    public String getHtmlContentFromPage(String url){
        StringBuilder builder = new StringBuilder();
        try {
            Document doc = Jsoup.connect(url).timeout(40 * 10000)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();

            Elements links = doc.select("a");
            for (Element link : links ){
                builder.append(link);
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return builder.toString();
    }

    public Integer getResponseCode(String urlPage) throws IOException {
        URL url = new URL(urlPage);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getResponseCode();
    }

    public String getPageTitle(){

        String title = null;
        try{
            Document doc = Jsoup.connect(url).get();
            title = doc.title();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return  title;
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

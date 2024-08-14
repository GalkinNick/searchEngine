package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
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
        Set<SiteCrawler> tasks = new HashSet<>();
        try {
            Thread.sleep(1500);
            for (String link : connect()){
                if (!link.trim().equals(url)){
                    resltSet.add(link);
                }
                else {
                    resltSet.add(link);
                    SiteCrawler crawler = new SiteCrawler(link.trim());
                    crawler.fork();
                    tasks.add(crawler);
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
            for (SiteCrawler parser : tasks){
                resltSet.addAll(parser.join());
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return resltSet;
    }
}

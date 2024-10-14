package searchengine.services;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.*;

public class WordToLemmaConverter {

    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public WordToLemmaConverter() throws IOException {
        this.luceneMorphology = new RussianLuceneMorphology();
    }

    public HashMap<String, Integer> convertingIntoLemmas(String text) throws IOException {

        String[] words = arrayContainsRussianWorlds(text);
        HashMap<String, Integer> lemmas = new HashMap<>();

        for (String word : words){
            if (word.isBlank()) {
                continue;
            }

            List<String>  wordBaseForms = luceneMorphology.getMorphInfo(word);

            if (anyWordBaseBelongToParticle(wordBaseForms)){
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            if(normalForms.isEmpty()){
                continue;
            }

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)){
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }

    private String[] arrayContainsRussianWorlds(String text){
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean hasParticleProperty(String wordBase){
        for (String property : particlesNames){
            if(wordBase.toUpperCase().contains(property)){
                return true;
            }
        }
        return false;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms){
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    public Set<String> getLemmaSet(String text){
        String[] arrayText = arrayContainsRussianWorlds(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : arrayText){
            if (!word.isEmpty() && isCorrectWordForm(word)){
                List<String> wordBaseForm = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForm)){
                    continue;
                }
                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    private boolean isCorrectWordForm(String word){

        List<String> wordInfo = luceneMorphology.getMorphInfo(word);

        for (String morphInfo : wordInfo){
            if(morphInfo.matches(WORD_TYPE_REGEX)){
                return false;
            }
        }

        return true;
    }


    public String removeHtmlTag(String dirtyHtml){
        return Jsoup.clean(dirtyHtml, Safelist.none());
    }

}

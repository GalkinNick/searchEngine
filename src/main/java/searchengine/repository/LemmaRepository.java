package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    @Transactional
    @Query(value = "FROM LemmaEntity l WHERE l.lemma =:findLemma")
    List<LemmaEntity> getListLemmas(@Param("findLemma") String findLemma);


    @Transactional
    @Query(value = "FROM LemmaEntity l WHERE l.lemma =:findLemma")
    LemmaEntity getLemma(@Param("findLemma") String findLemma);
}

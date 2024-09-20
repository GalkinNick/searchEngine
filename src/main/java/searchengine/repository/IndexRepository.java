package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

    @Transactional
    @Query(value = "FROM IndexEntity i where i.lemmaId =:lemmaEntity")
    IndexEntity getIndexEntity(@Param("lemmaEntity") LemmaEntity lemmaEntity);

    @Transactional
    @Query(value = "SELECT i.rank FROM IndexEntity i WHERE i.lemmaId =:lemma")
    float getRank(@Param("lemma") LemmaEntity lemma);
}

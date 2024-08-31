package searchengine.repository;

import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import javax.transaction.Transactional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM PageEntity p WHERE p.id =:id")
    void deleteBySiteId(@Param("id") Long id);
}

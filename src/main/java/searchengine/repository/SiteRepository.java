package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Long> {

    @Transactional
    @Query(value = "SELECT s.id from SiteEntity s where s.url=:url")
    Long findByUrl(@Param("url") String url);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM SiteEntity s WHERE s.id =:id")
    void deleteById(@Param("id") Long id);

    @Transactional
    @Query(value = "FROM SiteEntity s Where s.status =:status")
    List<SiteEntity> findByStatus(@Param("status") String status);
}

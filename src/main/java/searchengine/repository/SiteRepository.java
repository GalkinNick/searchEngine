package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.Statuses;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {

    @Transactional
    @Query(value = "SELECT s.id from SiteEntity s where s.url=:url")
    Integer findIdByUrl(@Param("url") String url);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM SiteEntity s WHERE s.id =:id")
    void deleteById(@Param("id") Integer id);

    @Transactional
    @Query(value = "FROM SiteEntity s WHERE s.status =:status")
    List<SiteEntity> findByStatus(@Param("status") Statuses status);
}

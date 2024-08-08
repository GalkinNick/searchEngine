package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sites AS s WHERE s.id = `id`;", nativeQuery = true)
    void deleteSitesById(@Param("id") Integer id);

    @Query(value = "INSERT INTO sites (siteEntity);")
    void saveSite(SiteEntity siteEntity);
}

package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;


import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

 /*   @Modifying
    @Transactional
    @Query(value = "select * from sites s where s.status=:status")
    void findByStatus(@Param("status") String status);*/
}

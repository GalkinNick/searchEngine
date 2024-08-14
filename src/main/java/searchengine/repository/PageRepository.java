package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import javax.transaction.Transactional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

//    @Transactional
//    @Modifying
//    @Query(value = "DELETE FROM pages AS p WHERE p.id = `id`;", nativeQuery = true)
//    void deletePageById(@Param("id") Integer id);
//
//    @Query(value = "INSERT INTO `pages` (`id`, `site_id`, `path`, `code`, `content`) " +
//            "values (?1, ?2, ?3, ?4, ?5);")
//    void savePages(int id, int siteId, String path, int code, String content);
}

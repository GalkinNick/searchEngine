package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "pages", indexes = @Index(name = "path_index", columnList = "path", unique = true))
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntityId;
    @Column(name ="path", columnDefinition = "TEXT NOT NULL")
    private String path;
    @Column(name = "code", columnDefinition = "INT NOT NULL")
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT NOT NULL")
    private String content;
}

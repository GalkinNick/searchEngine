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

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "`site_id`", columnDefinition = "INT", nullable = false)
    private SiteEntity siteEntityId;

    @Column(name ="`path`", columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(name = "`code`", columnDefinition = "INT", nullable = false)
    private Integer code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;


}

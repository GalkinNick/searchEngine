package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "sites")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('DEFAULT', 'INDEXING','INDEXED','FAILED')", nullable = false)
    private Statuses status;

    @Column(name = "status_time", columnDefinition = "DATETIME NOT NULL", nullable = false)
    private LocalDateTime StatusTime;

    @Column(name="last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", columnDefinition = "VARCHAR(255) NOT NULL", nullable = false)
    private String url;

    @Column(name = "name", columnDefinition = "VARCHAR(255) NOT NULL", nullable = false)
    private String name;

    @OneToMany(mappedBy = "siteEntityId", cascade = CascadeType.REMOVE)
    private List<PageEntity> pages;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    private List<LemmaEntity> lemmas;

}

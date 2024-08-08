package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "sites")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "status", columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL")
    private Statuses status;
    @Column(name = "status_time", columnDefinition = "DATETIME NOT NULL")
    private LocalDateTime StatusTime;
    @Column(name="last_error", columnDefinition = "TEXT")
    private String lastError;
    @Column(name = "url", columnDefinition = "VARCHAR(255) NOT NULL")
    private String url;
    @Column(name = "name", columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

}

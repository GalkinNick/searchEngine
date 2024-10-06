package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "lemmas")
public class LemmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "`site_id`", columnDefinition = "INT", nullable = false)
    private SiteEntity site;

    @Column(name = "`lemma`", columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(name = "`frequency`", nullable = false)
    private int frequency;

    @OneToMany(mappedBy = "lemmaId", cascade = CascadeType.REMOVE)
    private List<IndexEntity> indices;


}

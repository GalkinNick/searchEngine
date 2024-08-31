package searchengine.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "indices")
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long page;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name ="lemma_id", nullable = false)
    private LemmaEntity lemma;

    @Column(name = "rank", nullable = false)
    private Float rank;

      /*  @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name ="page_id", nullable = false)
    private PageEntity page;*/

}

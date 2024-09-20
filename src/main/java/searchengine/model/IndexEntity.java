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
    private Integer id;

    @Column(name = "`page_id`")
    private Integer pagesId;

   /* @ManyToOne
    @JoinColumn(name = "page_id", columnDefinition = "INT", nullable = false)
    private PageEntity pageId;*/

   @ManyToOne
    @JoinColumn(name ="`lemma_id`", columnDefinition = "INTEGER", nullable = false)
    private LemmaEntity lemmaId;


    @Column(name = "`rank`", columnDefinition = "float", nullable = false)
    private float rank;

}

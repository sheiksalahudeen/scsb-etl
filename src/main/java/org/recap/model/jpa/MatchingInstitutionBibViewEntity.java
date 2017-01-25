package org.recap.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by premkb on 24/1/17.
 */
@Entity
@Table(name = "MATCHING_INSTBIB_V", schema = "RECAP", catalog = "")
public class MatchingInstitutionBibViewEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "BIBID")
    private String bibId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }
}

package org.recap.model.jaxb;

import org.recap.model.jaxb.marc.ContentType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bib implements Serializable {
    @XmlElement
    private String owningInstitutionId;
    @XmlElement
    private String owningInstitutionBibId;
    @XmlElement
    protected List<MatchingInstitutionBibIdType> matchingInstitutionBibId;

    @XmlElement(required = true, nillable = true)
    protected ContentType content;

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public List<MatchingInstitutionBibIdType> getMatchingInstitutionBibId() {
        return matchingInstitutionBibId;
    }

    public void setMatchingInstitutionBibId(List<MatchingInstitutionBibIdType> matchingInstitutionBibId) {
        this.matchingInstitutionBibId = matchingInstitutionBibId;
    }

    public ContentType getContent() {
        return content;
    }

    public void setContent(ContentType content) {
        this.content = content;
    }
}

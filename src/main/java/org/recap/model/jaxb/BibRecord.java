package org.recap.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */

@XmlRootElement
public class BibRecord {
    private String owningInstitutionId;
    private String content;
    private List<Holdings> holdings;

    @XmlElement
    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    @XmlElement
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlElement
    public List<Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holdings> holdings) {
        this.holdings = holdings;
    }
}

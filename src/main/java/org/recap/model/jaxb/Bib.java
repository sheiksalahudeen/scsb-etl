package org.recap.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class Bib {
    private String owningInstitutionId;
    private String content;


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

}

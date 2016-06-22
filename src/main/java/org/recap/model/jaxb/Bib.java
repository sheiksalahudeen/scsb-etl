package org.recap.model.jaxb;

import com.sun.xml.internal.txw2.annotation.XmlCDATA;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bib {
    @XmlElement
    private String owningInstitutionId;
    @XmlElement(required = true, nillable = true)
    protected ContentType content;


    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public ContentType getContent() {
        return content;
    }

    public void setContent(ContentType content) {
        this.content = content;
    }
}

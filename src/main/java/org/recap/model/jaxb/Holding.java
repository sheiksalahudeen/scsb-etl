package org.recap.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */

public class Holding {

    private String owningInstitutionHoldingsId;

    private String content;

    private List<Items> items;

    @XmlElement
    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    @XmlElement
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlElement
    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }
}

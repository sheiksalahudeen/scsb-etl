package org.recap.model.jaxb;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class Items {

    private String content;

    @XmlElement
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

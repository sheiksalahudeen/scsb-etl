package org.recap.model.jpa;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by peris on 7/17/16.
 */

@Entity
@Table(name = "xml_records_t", schema = "recap", catalog = "")
public class XmlRecordEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Lob
    @Column(name = "xml_record")
    private String xml;

    @Column(name = "xml_file")
    private String xmlFileName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }
}

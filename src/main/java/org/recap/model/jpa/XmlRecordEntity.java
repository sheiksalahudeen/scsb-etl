package org.recap.model.jpa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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

    @Column(name="owning_inst")
    private String owningInst;

    @Column(name="owning_inst_bib_id")
    private String owningInstBibId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_loaded")
    private Date dataLoaded;

    public Date getDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(Date dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    public String getOwningInstBibId() {
        return owningInstBibId;
    }

    public void setOwningInstBibId(String owningInstBibId) {
        this.owningInstBibId = owningInstBibId;
    }

    public String getOwningInst() {
        return owningInst;
    }

    public void setOwningInst(String owningInst) {
        this.owningInst = owningInst;
    }

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

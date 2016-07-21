package org.recap.model.jpa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by hemalathas on 21/7/16.
 */
@Entity
@Table(name = "etl_gfa_temp_t" , schema = "recap")
public class EtlGfaEntity implements Serializable {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "ITEM_BARCODE")
    private String itemBarcode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ACCESSION_DATE")
    private Date accessionDate;

    @Column(name = "CUSTOMER")
    private String customer;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DELETE_DATE")
    private Date deleteDate;

    @Column(name = "STATUS")
    private String status;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public Date getAccessionDate() {
        return accessionDate;
    }

    public void setAccessionDate(Date accessionDate) {
        this.accessionDate = accessionDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

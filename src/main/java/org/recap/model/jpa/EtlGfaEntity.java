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
    @Column(name = "ITEM_BARCODE")
    private String itemBarcode;

    @Column(name = "CUSTOMER_CODE")
    private String customer;

    @Column(name = "ITEM_STATUS")
    private String status;

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

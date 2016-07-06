package org.recap.model.etl;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenchulakshmig on 30/6/16.
 */
public class LoadReportEntity implements Serializable {

    private String owningInstitution;
    private String owningInstitutionBibId;
    private String owningInstitutionHoldingsId;
    private String localItemId;
    private String itemBarcode;
    private String customerCode;
    private String title;
    private String collectionGroupDesignation;
    private Date createDateItem;
    private Date lastUpdatedDateItem;
    private String exceptionMessage;
    private String errorDescription;

    public String getOwningInstitution() {
        return owningInstitution;
    }

    public void setOwningInstitution(String owningInstitution) {
        this.owningInstitution = owningInstitution;
    }

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    public String getLocalItemId() {
        return localItemId;
    }

    public void setLocalItemId(String localItemId) {
        this.localItemId = localItemId;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCollectionGroupDesignation() {
        return collectionGroupDesignation;
    }

    public void setCollectionGroupDesignation(String collectionGroupDesignation) {
        this.collectionGroupDesignation = collectionGroupDesignation;
    }

    public Date getCreateDateItem() {
        return createDateItem;
    }

    public void setCreateDateItem(Date createDateItem) {
        this.createDateItem = createDateItem;
    }

    public Date getLastUpdatedDateItem() {
        return lastUpdatedDateItem;
    }

    public void setLastUpdatedDateItem(Date lastUpdatedDateItem) {
        this.lastUpdatedDateItem = lastUpdatedDateItem;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}

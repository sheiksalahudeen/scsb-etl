package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.util.Date;

/**
 * Created by peris on 7/21/16.
 */

@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX")
public class FailureReportReCAPCSVRecord  {
    @DataField(pos = 1)
    private String owningInstitution;
    @DataField(pos = 2)
    private String owningInstitutionBibId;
    @DataField(pos = 3)
    private String owningInstitutionHoldingsId;
    @DataField(pos = 4)
    private String localItemId;
    @DataField(pos = 5)
    private String itemBarcode;
    @DataField(pos = 6)
    private String customerCode;
    @DataField(pos = 7)
    private String title;
    @DataField(pos = 8)
    private String collectionGroupDesignation;
    @DataField(pos = 9)
    private String createDateItem;
    @DataField(pos = 10)
    private String lastUpdatedDateItem;
    @DataField(pos = 11)
    private String exceptionMessage;
    @DataField(pos = 12)
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

    public String getCreateDateItem() {
        return createDateItem;
    }

    public void setCreateDateItem(String createDateItem) {
        this.createDateItem = createDateItem;
    }

    public String getLastUpdatedDateItem() {
        return lastUpdatedDateItem;
    }

    public void setLastUpdatedDateItem(String lastUpdatedDateItem) {
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

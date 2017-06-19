package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

/**
 * Created by peris on 7/21/16.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX")
public class FailureReportReCAPCSVRecord implements Serializable{
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

    /**
     * Gets owning institution.
     *
     * @return the owning institution
     */
    public String getOwningInstitution() {
        return owningInstitution;
    }

    /**
     * Sets owning institution.
     *
     * @param owningInstitution the owning institution
     */
    public void setOwningInstitution(String owningInstitution) {
        this.owningInstitution = owningInstitution;
    }

    /**
     * Gets owning institution bib id.
     *
     * @return the owning institution bib id
     */
    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    /**
     * Sets owning institution bib id.
     *
     * @param owningInstitutionBibId the owning institution bib id
     */
    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    /**
     * Gets owning institution holdings id.
     *
     * @return the owning institution holdings id
     */
    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    /**
     * Sets owning institution holdings id.
     *
     * @param owningInstitutionHoldingsId the owning institution holdings id
     */
    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    /**
     * Gets local item id.
     *
     * @return the local item id
     */
    public String getLocalItemId() {
        return localItemId;
    }

    /**
     * Sets local item id.
     *
     * @param localItemId the local item id
     */
    public void setLocalItemId(String localItemId) {
        this.localItemId = localItemId;
    }

    /**
     * Gets item barcode.
     *
     * @return the item barcode
     */
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     * Sets item barcode.
     *
     * @param itemBarcode the item barcode
     */
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets collection group designation.
     *
     * @return the collection group designation
     */
    public String getCollectionGroupDesignation() {
        return collectionGroupDesignation;
    }

    /**
     * Sets collection group designation.
     *
     * @param collectionGroupDesignation the collection group designation
     */
    public void setCollectionGroupDesignation(String collectionGroupDesignation) {
        this.collectionGroupDesignation = collectionGroupDesignation;
    }

    /**
     * Gets create date item.
     *
     * @return the create date item
     */
    public String getCreateDateItem() {
        return createDateItem;
    }

    /**
     * Sets create date item.
     *
     * @param createDateItem the create date item
     */
    public void setCreateDateItem(String createDateItem) {
        this.createDateItem = createDateItem;
    }

    /**
     * Gets last updated date item.
     *
     * @return the last updated date item
     */
    public String getLastUpdatedDateItem() {
        return lastUpdatedDateItem;
    }

    /**
     * Sets last updated date item.
     *
     * @param lastUpdatedDateItem the last updated date item
     */
    public void setLastUpdatedDateItem(String lastUpdatedDateItem) {
        this.lastUpdatedDateItem = lastUpdatedDateItem;
    }

    /**
     * Gets exception message.
     *
     * @return the exception message
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * Sets exception message.
     *
     * @param exceptionMessage the exception message
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * Gets error description.
     *
     * @return the error description
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Sets error description.
     *
     * @param errorDescription the error description
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}

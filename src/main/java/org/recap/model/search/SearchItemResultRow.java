package org.recap.model.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by rajesh on 18-Jul-16.
 */
@ApiModel(value="SearchItemResultRow", description="Model for Displaying Item Result")
public class SearchItemResultRow implements Comparable<SearchItemResultRow> {

    @ApiModelProperty(name= "callNumber", value= "Call Number",position = 0)
    private String callNumber;
    @ApiModelProperty(name= "chronologyAndEnum", value= "Chronology And Enum",position = 1)
    private String chronologyAndEnum;
    @ApiModelProperty(name= "customerCode", value= "Customer Code",position = 2)
    private String customerCode;
    @ApiModelProperty(name= "barcode", value= "barcode",position = 3)
    private String barcode;
    @ApiModelProperty(name= "useRestriction", value= "use Restriction",position = 4)
    private String useRestriction;
    @ApiModelProperty(name= "collectionGroupDesignation", value= "collection Group Designation",position = 5)
    private String collectionGroupDesignation;
    @ApiModelProperty(name= "availability", value= "Availability",position = 6)
    private String availability;
    @ApiModelProperty(name= "selectedItem", value= "selected Item",position = 7)
    private boolean selectedItem = false;

    /**
     * Gets call number.
     *
     * @return the call number
     */
    public String getCallNumber() {
        return callNumber;
    }

    /**
     * Sets call number.
     *
     * @param callNumber the call number
     */
    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    /**
     * Gets chronology and enum.
     *
     * @return the chronology and enum
     */
    public String getChronologyAndEnum() {
        return chronologyAndEnum;
    }

    /**
     * Sets chronology and enum.
     *
     * @param chronologyAndEnum the chronology and enum
     */
    public void setChronologyAndEnum(String chronologyAndEnum) {
        this.chronologyAndEnum = chronologyAndEnum;
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
     * Gets barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets barcode.
     *
     * @param barcode the barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Gets use restriction.
     *
     * @return the use restriction
     */
    public String getUseRestriction() {
        return useRestriction;
    }

    /**
     * Sets use restriction.
     *
     * @param useRestriction the use restriction
     */
    public void setUseRestriction(String useRestriction) {
        this.useRestriction = useRestriction;
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
     * Gets availability.
     *
     * @return the availability
     */
    public String getAvailability() {
        return availability;
    }

    /**
     * Sets availability.
     *
     * @param availability the availability
     */
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * Is selected item boolean.
     *
     * @return the boolean
     */
    public boolean isSelectedItem() {
        return selectedItem;
    }

    /**
     * Sets selected item.
     *
     * @param selectedItem the selected item
     */
    public void setSelectedItem(boolean selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public int compareTo(SearchItemResultRow searchItemResultRow) {
        return this.getChronologyAndEnum().compareTo(searchItemResultRow.getChronologyAndEnum());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SearchItemResultRow))
            return false;
        SearchItemResultRow searchItemResultRow = (SearchItemResultRow) o;
        return chronologyAndEnum.equals(searchItemResultRow.chronologyAndEnum);
    }

    @Override
    public int hashCode() {
        return chronologyAndEnum.hashCode();
    }

}

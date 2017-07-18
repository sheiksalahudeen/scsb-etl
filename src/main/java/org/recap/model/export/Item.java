package org.recap.model.export;

import java.io.Serializable;

/**
 * Created by premkb on 12/7/17.
 */
public class Item implements Serializable {

    private String itemId;

    private String owningInstitutionItemId;

    private String barcode;

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets owning institution item id.
     *
     * @return the owning institution item id
     */
    public String getOwningInstitutionItemId() {
        return owningInstitutionItemId;
    }

    /**
     * Sets owning institution item id.
     *
     * @param owningInstitutionItemId the owning institution item id
     */
    public void setOwningInstitutionItemId(String owningInstitutionItemId) {
        this.owningInstitutionItemId = owningInstitutionItemId;
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
}

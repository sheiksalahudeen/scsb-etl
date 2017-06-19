package org.recap.model.export;

import java.io.Serializable;
import java.util.List;

/**
 * Created by premkb on 29/9/16.
 */
public class DeletedRecord implements Serializable{

    private String bibId;

    private List<String> itemBarcodes;

    /**
     * Gets bib id.
     *
     * @return the bib id
     */
    public String getBibId() {
        return bibId;
    }

    /**
     * Sets bib id.
     *
     * @param bibId the bib id
     */
    public void setBibId(String bibId) {
        this.bibId = bibId;
    }

    /**
     * Gets item barcodes.
     *
     * @return the item barcodes
     */
    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    /**
     * Sets item barcodes.
     *
     * @param itemBarcodes the item barcodes
     */
    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }
}

package org.recap.model.export;

import java.io.Serializable;
import java.util.List;

/**
 * Created by premkb on 12/7/17.
 */
public class Bib implements Serializable {

    private String bibId;

    private String owningInstitutionBibId;

    private String owningInstitutionCode;

    private boolean deleteAllItems;

    private List<Item> items;

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
     * Gets owning institution code.
     *
     * @return the owning institution code
     */
    public String getOwningInstitutionCode() {
        return owningInstitutionCode;
    }

    /**
     * Sets owning institution code.
     *
     * @param owningInstitutionCode the owning institution code
     */
    public void setOwningInstitutionCode(String owningInstitutionCode) {
        this.owningInstitutionCode = owningInstitutionCode;
    }

    /**
     * Is delete all items boolean.
     *
     * @return the boolean
     */
    public boolean isDeleteAllItems() {
        return deleteAllItems;
    }

    /**
     * Sets delete all items.
     *
     * @param deleteAllItems the delete all items
     */
    public void setDeleteAllItems(boolean deleteAllItems) {
        this.deleteAllItems = deleteAllItems;
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }
}

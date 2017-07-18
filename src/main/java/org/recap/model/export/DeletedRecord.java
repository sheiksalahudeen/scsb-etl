package org.recap.model.export;

import java.io.Serializable;
import java.util.List;

/**
 * Created by premkb on 29/9/16.
 */
public class DeletedRecord implements Serializable{

    private Bib bib;

    /**
     * Gets bib.
     *
     * @return the bib
     */
    public Bib getBib() {
        return bib;
    }

    /**
     * Sets bib.
     *
     * @param bib the bib
     */
    public void setBib(Bib bib) {
        this.bib = bib;
    }

}

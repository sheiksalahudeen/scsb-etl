package org.recap.model;

import org.recap.model.jaxb.BibRecord;
import org.recap.model.jpa.BibliographicEntity;

import java.util.Date;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class BibliographicEntityGenerator {
    public BibliographicEntity generateBibliographicEntity(BibRecord bibRecord){
        BibliographicEntity bibliographicEntity = new BibliographicEntity();

        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setContent(bibRecord.getContent());

        return bibliographicEntity;
    }
}

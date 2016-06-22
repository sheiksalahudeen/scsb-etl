package org.recap.model;

import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jpa.BibliographicEntity;

import java.util.Date;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class BibliographicEntityGenerator {
    public BibliographicEntity generateBibliographicEntity(BibRecord bibRecord){
        BibliographicEntity bibliographicEntity = new BibliographicEntity();

        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(bibRecord.getBib().getOwningInstitutionId());
        bibliographicEntity.setOwningInstitutionId(101);
        ContentType content = bibRecord.getBib().getContent();
        CollectionType collection = content.getCollection();
        String xmlContent = collection.serialize(collection);


        bibliographicEntity.setContent(xmlContent);

        return bibliographicEntity;
    }
}

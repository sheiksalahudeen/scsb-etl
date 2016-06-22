package org.recap.model;

import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.util.MarcUtil;

import java.util.Date;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class BibliographicEntityGenerator {

    private InstitutionDetailsRepository institutionDetailsRepository;
    private MarcUtil marcUtil;

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public BibliographicEntity generateBibliographicEntity(BibRecord bibRecord){
        BibliographicEntity bibliographicEntity = new BibliographicEntity();

        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(null == bibRecord.getBib().getOwningInstitutionBibId() ? getControlFieldValue001(bibRecord) : bibRecord.getBib().getOwningInstitutionBibId());
        bibliographicEntity.setOwningInstitutionId(institutionDetailsRepository.findByInstitutionCode(bibRecord.getBib().getOwningInstitutionId()).getInstitutionId());
        ContentType content = bibRecord.getBib().getContent();

        CollectionType collection = content.getCollection();
        String xmlContent = collection.serialize(collection);


        bibliographicEntity.setContent(xmlContent);

        return bibliographicEntity;
    }

    private String getControlFieldValue001(BibRecord bibRecord) {
        RecordType marcRecord = bibRecord.getBib().getContent().getCollection().getRecord().get(0);
        return getMarcUtil().getControlFieldValue(marcRecord, "001");
    }

    public MarcUtil getMarcUtil() {
        if (null == marcUtil) {
            marcUtil = new MarcUtil();
        }
        return marcUtil;
    }

    public void setMarcUtil(MarcUtil marcUtil) {
        this.marcUtil = marcUtil;
    }
}

package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.etl.BibAndRelatedInfoGenerator;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.util.BibSynchronzePersistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private Logger logger = LoggerFactory.getLogger(RecordProcessor.class);
    private JAXBHandler jaxbHandler;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private BibAndRelatedInfoGenerator bibAndRelatedInfoGenerator;
    private BibSynchronzePersistanceUtil bibSynchronzePersistanceUtil;


    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);

                BibliographicEntity bibliographicEntity = getBibAndRelatedInfoGenerator().generateBibAndRelatedInfo(bibRecord);
                bibliographicEntities.add(bibliographicEntity);
            }

            getBibSynchronzePersistanceUtil().saveBibRecords(bibliographicEntities);

        }
    }

    private BibSynchronzePersistanceUtil getBibSynchronzePersistanceUtil() {
        if(null == bibSynchronzePersistanceUtil) {
            bibSynchronzePersistanceUtil = BibSynchronzePersistanceUtil.getInstance();
            bibSynchronzePersistanceUtil.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        }
        return bibSynchronzePersistanceUtil;
    }

    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public BibAndRelatedInfoGenerator getBibAndRelatedInfoGenerator() {
        if (null == bibAndRelatedInfoGenerator) {
            bibAndRelatedInfoGenerator = new BibAndRelatedInfoGenerator();
        }
        return bibAndRelatedInfoGenerator;
    }

    public void setBibAndRelatedInfoGenerator(BibAndRelatedInfoGenerator bibAndRelatedInfoGenerator) {
        this.bibAndRelatedInfoGenerator = bibAndRelatedInfoGenerator;
    }
}

package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.BibliographicEntityGenerator;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.BibliographicHoldingsEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private  Logger logger = LoggerFactory.getLogger(RecordProcessor.class);
    private JAXBHandler jaxbHandler;
    private BibliographicEntityGenerator bibliographicEntityGenerator;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository;


    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            List<BibliographicHoldingsEntity> bibliographicHoldingsEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);
                BibliographicEntityGenerator bibliographicEntityGenerator = getBibliographicEntityGenerator();
                bibliographicEntityGenerator.setInstitutionDetailsRepository(institutionDetailsRepository);
                bibliographicHoldingsEntities.addAll(bibliographicEntityGenerator.generateBibliographicEntity(bibRecord));
            }
            long startTime = System.currentTimeMillis();
            bibliographicHoldingsDetailsRepository.save(bibliographicHoldingsEntities);
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to persist " + bibliographicHoldingsEntities.size() + " bibliographic entities is: " + (endTime-startTime)/1000 + " seconds");
        }
    }

    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
    }

    public BibliographicHoldingsDetailsRepository getBibliographicHoldingsDetailsRepository() {
        return bibliographicHoldingsDetailsRepository;
    }

    public void setBibliographicHoldingsDetailsRepository(BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository) {
        this.bibliographicHoldingsDetailsRepository = bibliographicHoldingsDetailsRepository;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public BibliographicEntityGenerator getBibliographicEntityGenerator() {
        if (null == bibliographicEntityGenerator) {
            bibliographicEntityGenerator = new BibliographicEntityGenerator();
        }
        return bibliographicEntityGenerator;
    }


}

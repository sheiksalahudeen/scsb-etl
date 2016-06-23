package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.BibliographicEntityInformationGeneratorCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private Logger logger = LoggerFactory.getLogger(RecordProcessor.class);
    private JAXBHandler jaxbHandler;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private BibliographicDetailsRepository bibliographicDetailsRepository;


    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Future> futures = new ArrayList<>();

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);
                Future future = executorService.submit(new BibliographicEntityInformationGeneratorCallable(bibRecord, institutionDetailsRepository));
                futures.add(future);
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                bibliographicEntities.add((BibliographicEntity) future.get());
            }

            long startTime = System.currentTimeMillis();
            bibliographicDetailsRepository.save(bibliographicEntities);
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to persist " + bibliographicEntities.size() + " bibliographic entities is: " + (endTime - startTime) / 1000 + " seconds");
        }
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
}

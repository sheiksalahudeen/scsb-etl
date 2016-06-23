package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.BibHoldingsGeneratorCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicHoldingsEntity;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
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
    private BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository;


    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Future> futures = new ArrayList<>();

            List<BibliographicHoldingsEntity> bibliographicHoldingsEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);
                Future future = executorService.submit(new BibHoldingsGeneratorCallable(bibRecord, institutionDetailsRepository));
                futures.add(future);
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                bibliographicHoldingsEntities.addAll((Collection<? extends BibliographicHoldingsEntity>) future.get());
            }

            long startTime = System.currentTimeMillis();
            bibliographicHoldingsDetailsRepository.save(bibliographicHoldingsEntities);
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to persist " + bibliographicHoldingsEntities.size() + " bibliographic entities is: " + (endTime - startTime) / 1000 + " seconds");
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
}

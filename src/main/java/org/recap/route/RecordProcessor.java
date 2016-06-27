package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.util.BibSynchronzePersistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    private BibSynchronzePersistanceUtil bibSynchronzePersistanceUtil;
    private ProducerTemplate producer;


    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            ExecutorService executorService = Executors.newFixedThreadPool(10);

            List<Future> futures = new ArrayList<>();

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);
                futures.add(executorService.submit(new BibPersisterCallable(bibRecord)));
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                BibliographicEntity bibliographicEntity = (BibliographicEntity) future.get();
                bibliographicEntities.add(bibliographicEntity);
            }

            producer.sendBody("activemq:queue:testQ", bibliographicEntities);

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

    public void setProducer(ProducerTemplate producer) {
        this.producer = producer;
    }
}

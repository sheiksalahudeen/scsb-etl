package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ItemStatusEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemStatusDetailsRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private Logger logger = LoggerFactory.getLogger(RecordProcessor.class);

    private JAXBHandler jaxbHandler;

    private ProducerTemplate producer;

    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private ItemStatusDetailsRespository itemStatusDetailsRespository;
    private Map institutionEntityMap;
    private Map itemStatusMap;

    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            ExecutorService executorService = Executors.newFixedThreadPool(15);

            List<Future> futures = new ArrayList<>();

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

            BibRecord bibRecord = null;
            for (String content : (List<String>) exchange.getIn().getBody()) {
                bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);

                futures.add(executorService.submit(new BibPersisterCallable(bibRecord, getInstitutionEntityMap(), getItemStatusMap())));
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                Object o = future.get();
                if (null != o) {
                    BibliographicEntity bibliographicEntity = (BibliographicEntity) o;
                    bibliographicEntities.add(bibliographicEntity);
                } else {
                    logger.error("No response from BibPersisterCallable" + bibRecord.getBib().getOwningInstitutionBibId());
                }
            }

            producer.sendBody("activemq:queue:testQ", bibliographicEntities);

        }
    }


    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
    }


    public ItemStatusDetailsRespository getItemStatusDetailsRespository() {
        return itemStatusDetailsRespository;
    }

    public void setItemStatusDetailsRespository(ItemStatusDetailsRespository itemStatusDetailsRespository) {
        this.itemStatusDetailsRespository = itemStatusDetailsRespository;
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

    public Map getInstitutionEntityMap() {
        if (null == institutionEntityMap) {
            institutionEntityMap = new HashMap();
            Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
            for (Iterator<InstitutionEntity> iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                InstitutionEntity institutionEntity = iterator.next();
                institutionEntityMap.put(institutionEntity.getInstitutionCode(), institutionEntity.getInstitutionId());
            }
        }
        return institutionEntityMap;
    }

    public Map getItemStatusMap() {
        if (null == itemStatusMap) {
            itemStatusMap = new HashMap();
            Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRespository.findAll();
            for (Iterator<ItemStatusEntity> iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                ItemStatusEntity itemStatusEntity = iterator.next();
                itemStatusMap.put(itemStatusEntity.getStatusCode(), itemStatusEntity.getItemStatusId());
            }
        }
        return itemStatusMap;
    }
}

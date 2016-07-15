package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.etl.LoadReportEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ItemStatusEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
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
    private ItemStatusDetailsRepository itemStatusDetailsRepository;
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;
    private Map institutionEntityMap;
    private Map itemStatusMap;
    private Map collectionGroupMap;

    @Override
    public void process(Exchange exchange) {
        ExecutorService executorService = null;
        if (exchange.getIn().getBody() instanceof List) {

            executorService = Executors.newFixedThreadPool(50);

            List<Future> futures = new ArrayList<>();

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
            List<LoadReportEntity> loadReportEntities = new ArrayList<>();

            BibRecord bibRecord = null;
            for (String content : (List<String>) exchange.getIn().getBody()) {
                bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);

                Future submit = executorService.submit(new BibPersisterCallable(bibRecord, getInstitutionEntityMap(), getItemStatusMap(), getCollectionGroupMap()));
                if (null != submit) {
                    futures.add(submit);
                }
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                Object object = null;
                try {
                    object = future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Map<String, Object> map = (Map<String, Object>) object;
                if (object != null) {
                    Object bibliographicEntity = map.get("bibliographicEntity");
                    Object loadReportEntity = map.get("loadReportEntity");
                    if (bibliographicEntity != null) {
                        bibliographicEntities.add((BibliographicEntity) bibliographicEntity);
                    } else if (loadReportEntity != null) {
                        loadReportEntities.add((LoadReportEntity) loadReportEntity);
                    }
                }
            }

            if (!CollectionUtils.isEmpty(bibliographicEntities)) {
                ETLExchange etlExchange = new ETLExchange();
                etlExchange.setBibliographicEntities(bibliographicEntities);
                etlExchange.setInstitutionEntityMap(getInstitutionEntityMap());
                etlExchange.setCollectionGroupMap(getCollectionGroupMap());
                producer.sendBody("activemq:queue:etlLoadQ", etlExchange);
            }
            if (!CollectionUtils.isEmpty(loadReportEntities)) {
                producer.sendBody("activemq:queue:etlReportQ", loadReportEntities);
            }

        }

        if (null != executorService) {
            executorService.shutdown();
        }
    }


    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
    }


    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    public void setItemStatusDetailsRepository(ItemStatusDetailsRepository itemStatusDetailsRepository) {
        this.itemStatusDetailsRepository = itemStatusDetailsRepository;
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

    public CollectionGroupDetailsRepository getCollectionGroupDetailsRepository() {
        return collectionGroupDetailsRepository;
    }

    public void setCollectionGroupDetailsRepository(CollectionGroupDetailsRepository collectionGroupDetailsRepository) {
        this.collectionGroupDetailsRepository = collectionGroupDetailsRepository;
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
            Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRepository.findAll();
            for (Iterator<ItemStatusEntity> iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                ItemStatusEntity itemStatusEntity = iterator.next();
                itemStatusMap.put(itemStatusEntity.getStatusCode(), itemStatusEntity.getItemStatusId());
            }
        }
        return itemStatusMap;
    }

    public Map getCollectionGroupMap() {
        if (null == collectionGroupMap) {
            collectionGroupMap = new HashMap();
            Iterable<CollectionGroupEntity> collectionGroupEntities = collectionGroupDetailsRepository.findAll();
            for (Iterator<CollectionGroupEntity> iterator = collectionGroupEntities.iterator(); iterator.hasNext(); ) {
                CollectionGroupEntity collectionGroupEntity = iterator.next();
                collectionGroupMap.put(collectionGroupEntity.getCollectionGroupCode(), collectionGroupEntity.getCollectionGroupId());
            }
        }
        return collectionGroupMap;
    }
}

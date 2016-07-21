package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.etl.LoadReportEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.util.CsvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by pvsubrah on 6/21/16.
 */

@Component
public class RecordProcessor {
    private Logger logger = LoggerFactory.getLogger(RecordProcessor.class);

    private Map institutionEntityMap;
    private Map itemStatusMap;
    private Map collectionGroupMap;
    private JAXBHandler jaxbHandler;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    CsvUtil csvUtil;

    @Autowired
    BibDataProcessor bibDataProcessor;

    private ExecutorService executorService;


    public void process(Page<XmlRecordEntity> xmlRecordEntities) {
        logger.info("Processor: " + Thread.currentThread().getName());

        List<Future> futures = new ArrayList<>();

        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
        List<LoadReportEntity> loadReportEntities = new ArrayList<>();

        BibRecord bibRecord = null;

        for (Iterator<XmlRecordEntity> iterator = xmlRecordEntities.iterator(); iterator.hasNext(); ) {
            XmlRecordEntity xmlRecordEntity = iterator.next();
            String xml = new String(xmlRecordEntity.getXml());

            bibRecord = (BibRecord) getJaxbHandler().unmarshal(xml, BibRecord.class);

            Future submit = getExecutorService().submit(new BibPersisterCallable(bibRecord, getInstitutionEntityMap(), getItemStatusMap(), getCollectionGroupMap()));
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
            bibDataProcessor.processETLExchagneAndPersistToDB(etlExchange);
        }

        if (!CollectionUtils.isEmpty(loadReportEntities)) {
            producer.sendBody("activemq:queue:etlReportQ", loadReportEntities);
        }

    }


    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
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

    public ExecutorService getExecutorService() {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(50);
        }
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void shutdownExecutorService() {
        getExecutorService().shutdown();
    }
}

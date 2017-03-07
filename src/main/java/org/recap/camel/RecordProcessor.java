package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.*;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.util.DBReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by pvsubrah on 6/21/16.
 */

@Component
public class RecordProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RecordProcessor.class);

    private Map institutionEntityMap;
    private Map itemStatusMap;
    private Map collectionGroupMap;
    private JAXBHandler jaxbHandler;
    private String xmlFileName;
    private String institutionName;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    BibDataProcessor bibDataProcessor;

    @Autowired
    DBReportUtil dbReportUtil;

    private ExecutorService executorService;


    public void process(Page<XmlRecordEntity> xmlRecordEntities) {
        logger.info("Processor: " + Thread.currentThread().getName());

        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();

        List<Future<Map<String, String>>> futures = prepareFutureTasks(xmlRecordEntities, reportEntities);

        for (Iterator<Future<Map<String, String>>> iterator = futures.iterator(); iterator.hasNext(); ) {
            Future future = iterator.next();
            Object object = null;
            try {
                object = future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(RecapConstants.ERROR,e);
            }

            processFutureResults(object, bibliographicEntities, reportEntities);
        }

        if (!CollectionUtils.isEmpty(bibliographicEntities)) {
            ETLExchange etlExchange = new ETLExchange();
            etlExchange.setBibliographicEntities(bibliographicEntities);
            etlExchange.setInstitutionEntityMap(getInstitutionEntityMap());
            etlExchange.setCollectionGroupMap(getCollectionGroupMap());
            bibDataProcessor.setXmlFileName(xmlFileName);
            bibDataProcessor.setInstitutionName(institutionName);
            bibDataProcessor.processETLExchagneAndPersistToDB(etlExchange);
        }


        if (!CollectionUtils.isEmpty(reportEntities)) {
            for(ReportEntity reportEntity : reportEntities) {
                producer.sendBody(RecapConstants.REPORT_Q, reportEntity);
            }
        }

    }

    private void processFutureResults(Object object, List<BibliographicEntity> bibliographicEntities, List<ReportEntity> reportEntities) {
        Map<String, Object> resultMap = (Map<String, Object>) object;

        if (object != null) {
            Object bibliographicEntity = resultMap.get("bibliographicEntity");
            List<ReportEntity> reportEntityList = (List<ReportEntity>) resultMap.get("reportEntities");
            if (bibliographicEntity != null) {
                bibliographicEntities.add((BibliographicEntity) bibliographicEntity);
            }
            if (!CollectionUtils.isEmpty(reportEntityList)) {
                reportEntities.addAll(reportEntityList);
            }
        }
    }

    private List<Future<Map<String, String>>> prepareFutureTasks(Page<XmlRecordEntity> xmlRecordEntities, List<ReportEntity> reportEntities) {
        BibRecord bibRecord;

        List<Callable<Map<String, String>>> callables = new ArrayList<>();

        for (Iterator<XmlRecordEntity> iterator = xmlRecordEntities.iterator(); iterator.hasNext(); ) {
            XmlRecordEntity xmlRecordEntity = iterator.next();
            String xml = new String(xmlRecordEntity.getXml());
            try {
                bibRecord = (BibRecord) getJaxbHandler().unmarshal(xml, BibRecord.class);
                BibPersisterCallable bibPersisterCallable = new BibPersisterCallable();
                bibPersisterCallable.setDbReportUtil(dbReportUtil);
                bibPersisterCallable.setBibRecord(bibRecord);
                bibPersisterCallable.setCollectionGroupMap(getCollectionGroupMap());
                bibPersisterCallable.setInstitutionEntitiesMap(getInstitutionEntityMap());
                bibPersisterCallable.setItemStatusMap(getItemStatusMap());
                bibPersisterCallable.setXmlRecordEntity(xmlRecordEntity);
                bibPersisterCallable.setInstitutionName(institutionName);

                callables.add(bibPersisterCallable);

            } catch (Exception e) {
                logger.error(RecapConstants.ERROR,e);
                ReportEntity reportEntity = new ReportEntity();
                List<ReportDataEntity> reportDataEntities = new ArrayList<>();
                String owningInst = xmlRecordEntity.getOwningInst();
                reportEntity.setCreatedDate(new Date());
                reportEntity.setType(RecapConstants.FAILURE);
                reportEntity.setFileName(xmlRecordEntity.getXmlFileName());
                reportEntity.setInstitutionName(owningInst);

                if(StringUtils.isNotBlank(owningInst)) {
                    ReportDataEntity reportDataEntity = new ReportDataEntity();
                    reportDataEntity.setHeaderName(RecapConstants.OWNING_INSTITUTION);
                    reportDataEntity.setHeaderValue(String.valueOf(getInstitutionEntityMap().get(owningInst)));
                    reportDataEntities.add(reportDataEntity);
                }

                if(StringUtils.isNotBlank(xmlRecordEntity.getOwningInstBibId())) {
                    ReportDataEntity reportDataEntity = new ReportDataEntity();
                    reportDataEntity.setHeaderName(RecapConstants.OWNING_INSTITUTION_BIB_ID);
                    reportDataEntity.setHeaderValue(xmlRecordEntity.getOwningInstBibId());
                    reportDataEntities.add(reportDataEntity);
                }

                if(e.getCause() != null) {
                    ReportDataEntity reportDataEntity = new ReportDataEntity();
                    reportDataEntity.setHeaderName(RecapConstants.EXCEPTION_MESSAGE);
                    reportDataEntity.setHeaderValue(e.getCause().getMessage());
                    reportDataEntities.add(reportDataEntity);
                }

                reportEntity.setReportDataEntities(reportDataEntities);
                reportEntities.add(reportEntity);
            }
        }

        List<Future<Map<String, String>>> futures = null;
        try {
            futures = getExecutorService().invokeAll(callables);
        } catch (InterruptedException e) {
            logger.error(RecapConstants.ERROR,e);
        }

        futures
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });

        return futures;
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

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void shutdownExecutorService() {
        getExecutorService().shutdown();
    }
}

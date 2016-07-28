package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.LoadReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class BibDataProcessor {

    Logger logger = LoggerFactory.getLogger(BibDataProcessor.class);

    private String xmlFileName;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void processETLExchagneAndPersistToDB(ETLExchange etlExchange) {
        if (etlExchange != null) {
            List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
            List<BibliographicEntity> bibliographicEntityList = etlExchange.getBibliographicEntities();

            try {
                bibliographicDetailsRepository.save(bibliographicEntityList);
                flushAndClearSession();
            } catch (Exception e) {
                LoadReportUtil loadReportUtil = new LoadReportUtil(etlExchange.getInstitutionEntityMap(), etlExchange.getCollectionGroupMap());
                for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                    try {
                        bibliographicDetailsRepository.save(bibliographicEntity);
                        flushAndClearSession();
                    } catch (Exception ex) {
                        List<FailureReportReCAPCSVRecord> failureReportEntities = processBibHoldingsItems(loadReportUtil, bibliographicEntity);
                        failureReportReCAPCSVRecords.addAll(failureReportEntities);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(failureReportReCAPCSVRecords)) {
                ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
                reCAPCSVRecord.setFailureReportReCAPCSVRecordList(failureReportReCAPCSVRecords);
                producer.sendBody("seda:etlFailureReportQ", reCAPCSVRecord);
            }
        }
    }

    private List<FailureReportReCAPCSVRecord> processBibHoldingsItems(LoadReportUtil loadReportUtil, BibliographicEntity bibliographicEntity) {
        List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
        List<HoldingsEntity> savedHoldingsEntities = new ArrayList<>();
        List<ItemEntity> savedItemEntities = new ArrayList<>();
        try {
            List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
            bibliographicEntity.setHoldingsEntities(null);
            bibliographicEntity.setItemEntities(null);

            bibliographicDetailsRepository.save(bibliographicEntity);
            flushAndClearSession();
            for (HoldingsEntity holdingsEntity : holdingsEntities) {
                List<ItemEntity> itemEntities = holdingsEntity.getItemEntities();
                holdingsEntity.setItemEntities(null);
                try {
                    HoldingsEntity savedHoldingsEntity = holdingsDetailsRepository.save(holdingsEntity);
                    flushAndClearSession();
                    savedHoldingsEntities.add(savedHoldingsEntity);
                    for (ItemEntity itemEntity : itemEntities) {
                        try {
                            itemEntity.setHoldingsEntity(savedHoldingsEntity);
                            ItemEntity savedItemEntity = itemDetailsRepository.save(itemEntity);
                            flushAndClearSession();
                            savedItemEntities.add(savedItemEntity);
                        } catch (Exception itemEx) {
                            FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = loadReportUtil.populateBibHoldingsItemInfo(bibliographicEntity, holdingsEntity, itemEntity);
                            failureReportReCAPCSVRecord.setExceptionMessage(itemEx.getCause().getCause().getMessage());
                            failureReportReCAPCSVRecord.setFileName(xmlFileName);
                            failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
                        }
                    }
                } catch (Exception holdingsEx) {
                    FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = loadReportUtil.populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
                    failureReportReCAPCSVRecord.setExceptionMessage(holdingsEx.getCause().getCause().getMessage());
                    failureReportReCAPCSVRecord.setFileName(xmlFileName);
                    failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
                }
            }
            bibliographicEntity.setHoldingsEntities(savedHoldingsEntities);
            bibliographicEntity.setItemEntities(savedItemEntities);
            bibliographicDetailsRepository.save(bibliographicEntity);
            flushAndClearSession();
        } catch (Exception bibEx) {
            FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = loadReportUtil.populateBibInfo(bibliographicEntity);
            failureReportReCAPCSVRecord.setExceptionMessage(bibEx.getCause().getCause().getMessage());
            failureReportReCAPCSVRecord.setFileName(xmlFileName);
            failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
        }
        return failureReportReCAPCSVRecords;
    }

    private void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }
}

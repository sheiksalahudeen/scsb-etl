package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.DBReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class BibDataProcessor {

    Logger logger = LoggerFactory.getLogger(BibDataProcessor.class);

    private String xmlFileName;

    private String institutionName;

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

    @Autowired
    DBReportUtil DBReportUtil;

    @Transactional
    public void processETLExchagneAndPersistToDB(ETLExchange etlExchange) {
        ReportEntity reportEntity = null;
        if (etlExchange != null) {
            List<BibliographicEntity> bibliographicEntityList = etlExchange.getBibliographicEntities();

            try {
                bibliographicDetailsRepository.save(bibliographicEntityList);
                flushAndClearSession();
            } catch (Exception e) {
                clearSession();
                DBReportUtil.setCollectionGroupMap(etlExchange.getCollectionGroupMap());
                DBReportUtil.setInstitutionEntitiesMap(etlExchange.getInstitutionEntityMap());
                for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                    try {
                        bibliographicDetailsRepository.save(bibliographicEntity);
                        flushAndClearSession();
                    } catch (Exception ex) {
                        clearSession();
                        reportEntity = processBibHoldingsItems(DBReportUtil, bibliographicEntity);
                    }
                }
            }
            if (null != reportEntity) {
                producer.sendBody(ReCAPConstants.REPORT_Q, reportEntity);
            }
        }
    }

    public ReportEntity processBibHoldingsItems(DBReportUtil dbReportUtil, BibliographicEntity bibliographicEntity) {
        ReportEntity reportEntity = new ReportEntity();

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
                            clearSession();
                            List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsAndItemsFailureReportEntities(bibliographicEntity, holdingsEntity, itemEntity);
                            ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
                            exceptionReportDataEntity.setHeaderName(ReCAPConstants.EXCEPTION_MESSAGE);
                            exceptionReportDataEntity.setHeaderValue(itemEx.getCause().getCause().getMessage());
                            reportDataEntities.add(exceptionReportDataEntity);

                            reportEntity.setReportDataEntities(reportDataEntities);
                            reportEntity.setFileName(xmlFileName);
                            reportEntity.setCreatedDate(new Date());
                            reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
                            reportEntity.setInstitutionName(institutionName);
                        }
                    }
                } catch (Exception holdingsEx) {
                    clearSession();
                    List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsFailureReportEntity(bibliographicEntity, holdingsEntity);
                    reportEntity.setReportDataEntities(reportDataEntities);
                    ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
                    exceptionReportDataEntity.setHeaderName(ReCAPConstants.EXCEPTION_MESSAGE);
                    exceptionReportDataEntity.setHeaderValue(holdingsEx.getCause().getCause().getMessage());
                    reportDataEntities.add(exceptionReportDataEntity);
                    reportEntity.setFileName(xmlFileName);
                    reportEntity.setCreatedDate(new Date());
                    reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
                    reportEntity.setInstitutionName(institutionName);
                }
            }
            bibliographicEntity.setHoldingsEntities(savedHoldingsEntities);
            bibliographicEntity.setItemEntities(savedItemEntities);
            bibliographicDetailsRepository.save(bibliographicEntity);
            flushAndClearSession();
        } catch (Exception bibEx) {
            clearSession();
            List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibFailureReportEntity(bibliographicEntity);

            ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
            exceptionReportDataEntity.setHeaderName(ReCAPConstants.EXCEPTION_MESSAGE);

            if(bibEx.getCause() != null && bibEx.getCause().getCause() != null) {
                exceptionReportDataEntity.setHeaderValue(bibEx.getCause().getCause().getMessage());
            } else {
                exceptionReportDataEntity.setHeaderValue(bibEx.getMessage());
            }
            reportDataEntities.add(exceptionReportDataEntity);
            reportEntity.setFileName(xmlFileName);
            reportEntity.setCreatedDate(new Date());
            reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
            reportEntity.setInstitutionName(institutionName);
        }
        return reportEntity;
    }

    private void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    private void clearSession() {
        entityManager.clear();
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
}

package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.recap.RecapConstants;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.DBReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class BibDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BibDataProcessor.class);

    private String xmlFileName;

    private String institutionName;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private DBReportUtil dbReportUtil;

    @Autowired
    private EtlDataLoadDAOService etlDataLoadDAOService;

    /**
     * This method persists the bibliographic entities. If errors encountered prepares report entities and sends to report queue for persisting.
     *
     * @param etlExchange
     */
    public void processETLExchagneAndPersistToDB(ETLExchange etlExchange) {
        ReportEntity reportEntity = null;
        if (etlExchange != null) {
            List<BibliographicEntity> bibliographicEntityList = etlExchange.getBibliographicEntities();
            try {
                etlDataLoadDAOService.saveBibliographicEntityList(bibliographicEntityList);
            } catch (PersistenceException pe){
                try {
                    reportEntity = processRecordWhenDuplicateBarcodeException(reportEntity, bibliographicEntityList, pe);
                } catch(Exception e) {
                    logger.info("exception while eliminating..");
                    logger.error(RecapConstants.ERROR,e);
                }
            } catch (Exception e) {
                logger.error(RecapConstants.ERROR,e);
                etlDataLoadDAOService.clearSession();
                dbReportUtil.setCollectionGroupMap(etlExchange.getCollectionGroupMap());
                dbReportUtil.setInstitutionEntitiesMap(etlExchange.getInstitutionEntityMap());
                for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                    try {
                        etlDataLoadDAOService.saveBibliographicEntity(bibliographicEntity);
                    } catch (Exception ex) {
                        logger.error(RecapConstants.ERROR,ex);
                        etlDataLoadDAOService.clearSession();
                        reportEntity = processBibHoldingsItems(dbReportUtil, bibliographicEntity);
                    }
                }
            }
            if (null != reportEntity) {
                producer.sendBody(RecapConstants.REPORT_Q, reportEntity);
            }
        }
    }

    private ReportEntity processRecordWhenDuplicateBarcodeException(ReportEntity reportEntity, List<BibliographicEntity> bibliographicEntityList, PersistenceException pe) {
        logger.error("persistence exe-->",pe);
        etlDataLoadDAOService.clearSession();
        String constraint = ((ConstraintViolationException)pe.getCause()).getConstraintName();
        String message = (pe.getCause()).getCause().getMessage();
        String barcodeOwnInst = "";
        barcodeOwnInst = StringUtils.removeStart(message,"Duplicate entry '");
        barcodeOwnInst = StringUtils.removeEnd(barcodeOwnInst,"' for key 'BARCODE'");
        String[] barcodeOwnInstArray = barcodeOwnInst.split("-");
        logger.info("Duplicated barcode--->{} in file {}",barcodeOwnInstArray[0],getXmlFileName());
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            reportEntity = processDuplicatedRecord(reportEntity, barcodeOwnInstArray[0], bibliographicEntity);
        }
        return reportEntity;
    }

    private ReportEntity processDuplicatedRecord(ReportEntity reportEntity, String barcode, BibliographicEntity bibliographicEntity) {
        bibliographicEntity.setItemEntities(new ArrayList<>());
        List<HoldingsEntity> updatedHoldingEntityList = new ArrayList<>();
        for (HoldingsEntity holdingsEntity:bibliographicEntity.getHoldingsEntities()){
            List<ItemEntity> itemEntityListWithNoDuplicatedBarcode = new ArrayList<>();
            for (ItemEntity itemEntity:holdingsEntity.getItemEntities()){
                List<ItemEntity> existingItemEntityList = itemDetailsRepository.findByBarcode(itemEntity.getBarcode());
                logger.info("Processing own bib {}, own hold id {}, own item id {}",bibliographicEntity.getOwningInstitutionBibId(),
                        holdingsEntity.getOwningInstitutionHoldingsId(),itemEntity.getOwningInstitutionItemId());
                logger.info("duplicated barcode--> {} current item barcode--->{}, existingItemEntityList size-->{}",barcode,itemEntity.getBarcode(),existingItemEntityList.size());
                if(!existingItemEntityList.isEmpty()){//Add to report if duplicate barcode found
                    logger.info("existing barcode--->{}",existingItemEntityList.get(0).getBarcode());
                    ItemEntity existingItemEntity = existingItemEntityList.get(0);
                    reportEntity = setDuplicateBarcodeReportInfo(itemEntity.getBarcode(),existingItemEntity,dbReportUtil,bibliographicEntity, reportEntity, holdingsEntity, itemEntity);
                } else {
                    logger.info("Duplicate not found for the barcode in other bib, barcode-->{}",itemEntity.getBarcode());
                    ItemEntity existingItemEntity = getExistingBarcodeItemWithinSameBib(itemEntityListWithNoDuplicatedBarcode,itemEntity);//If the item is not available in db but barcode is duplicated in the same bib,so this is to find the barcode already added to the bib to save, if the barcode is already there the current item which is having same barcode will be eliminated by below conditions
                    if (existingItemEntity == null) {
                        logger.info("Added non duplicate item to list to save, barcode-->{}",itemEntity.getBarcode());
                        itemEntityListWithNoDuplicatedBarcode.add(itemEntity);
                    } else {
                        logger.info("Setting report for duplicate barcode for item with in same bib, barcode-->{}",itemEntity.getBarcode());
                        reportEntity = setDuplicateBarcodeReportInfoForItemsinSameBib(existingItemEntity.getBarcode(),existingItemEntity,dbReportUtil,bibliographicEntity, reportEntity, holdingsEntity, itemEntity);
                    }
                }
            }
            if (!itemEntityListWithNoDuplicatedBarcode.isEmpty()) {
                holdingsEntity.setItemEntities(itemEntityListWithNoDuplicatedBarcode);
                updatedHoldingEntityList.add(holdingsEntity);
                bibliographicEntity.getItemEntities().addAll(itemEntityListWithNoDuplicatedBarcode);
            }
        }
        if (!updatedHoldingEntityList.isEmpty()) {
            bibliographicEntity.setHoldingsEntities(updatedHoldingEntityList);
            logger.info("saving bib id--->{} item ids - barcodes-->{}",bibliographicEntity.getOwningInstitutionBibId(),getBarcodeList(bibliographicEntity.getItemEntities()));
            etlDataLoadDAOService.saveBibliographicEntity(bibliographicEntity);
        }
        return reportEntity;
    }

    private String getBarcodeList(List<ItemEntity> itemEntityList){
        StringBuilder stringBuilder= new StringBuilder();
        for(ItemEntity itemEntity:itemEntityList){
            stringBuilder.append(itemEntity.getOwningInstitutionItemId()).append("-").append(itemEntity.getBarcode()).append(",");
        }
        return stringBuilder.toString();
    }

    private ItemEntity getExistingBarcodeItemWithinSameBib(List<ItemEntity> existingItemList,ItemEntity itemEntity){
        for(ItemEntity existingItem:existingItemList){
            if(existingItem.getBarcode().equals(itemEntity.getBarcode())){
                return existingItem;
            }
        }
        return null;
    }

    private ReportEntity setDuplicateBarcodeReportInfo(String barcode, ItemEntity existingItemEntity,DBReportUtil dbReportUtil, BibliographicEntity bibliographicEntity, ReportEntity reportEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity){
        String failureMessage = "Item barcode "+barcode+" is duplicated, existing record info owning inst bib id "
                +existingItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId()
                +", owning inst holding id "+existingItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()
                +", owning inst item id "+existingItemEntity.getOwningInstitutionItemId();
        reportEntity = new ReportEntity();
        setItemFailureReportInfo(dbReportUtil, bibliographicEntity, reportEntity, holdingsEntity, itemEntity, null,failureMessage);
        return reportEntity;
    }

    private ReportEntity setDuplicateBarcodeReportInfoForItemsinSameBib(String barcode, ItemEntity existingItemEntity,DBReportUtil dbReportUtil, BibliographicEntity bibliographicEntity, ReportEntity reportEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity){
        String failureMessage = "Item barcode "+barcode+" is duplicated, existing record info owning inst bib id "
                +bibliographicEntity.getOwningInstitutionBibId()
                +", owning inst holding id "+holdingsEntity.getOwningInstitutionHoldingsId()
                +", owning inst item id "+existingItemEntity.getOwningInstitutionItemId();
        reportEntity = new ReportEntity();
        setItemFailureReportInfo(dbReportUtil, bibliographicEntity, reportEntity, holdingsEntity, itemEntity, null,failureMessage);
        return reportEntity;
    }
    /**
     * Persists bibliographic entities and returns a report entity if an exception is encountered.
     *
     * @param dbReportUtil
     * @param bibliographicEntity
     * @return
     */
    public ReportEntity processBibHoldingsItems(DBReportUtil dbReportUtil, BibliographicEntity bibliographicEntity) {
        ReportEntity reportEntity = new ReportEntity();

        List<HoldingsEntity> savedHoldingsEntities = new ArrayList<>();
        List<ItemEntity> savedItemEntities = new ArrayList<>();

        try {
            List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
            bibliographicEntity.setHoldingsEntities(null);
            bibliographicEntity.setItemEntities(null);

            etlDataLoadDAOService.saveBibliographicEntity(bibliographicEntity);
            for (HoldingsEntity holdingsEntity : holdingsEntities) {
                List<ItemEntity> itemEntities = holdingsEntity.getItemEntities();
                holdingsEntity.setItemEntities(null);
                try {
                    HoldingsEntity savedHoldingsEntity = etlDataLoadDAOService.savedHoldingsEntity(holdingsEntity);
                    savedHoldingsEntities.add(savedHoldingsEntity);
                    for (ItemEntity itemEntity : itemEntities) {
                        try {
                            itemEntity.setHoldingsEntities(Arrays.asList(savedHoldingsEntity));
                            ItemEntity savedItemEntity = etlDataLoadDAOService.saveItemEntity(itemEntity);
                            savedItemEntities.add(savedItemEntity);
                        } catch (Exception itemEx) {
                            logger.error(RecapConstants.ERROR,itemEx);
                            etlDataLoadDAOService.clearSession();
                            setItemFailureReportInfo(dbReportUtil, bibliographicEntity, reportEntity, holdingsEntity, itemEntity, itemEx,null);
                        }
                    }
                } catch (Exception holdingsEx) {
                    logger.error(RecapConstants.ERROR,holdingsEx);
                    etlDataLoadDAOService.clearSession();
                    List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsFailureReportEntity(bibliographicEntity, holdingsEntity);
                    reportEntity.setReportDataEntities(reportDataEntities);
                    ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
                    exceptionReportDataEntity.setHeaderName(RecapConstants.EXCEPTION_MESSAGE);
                    exceptionReportDataEntity.setHeaderValue(holdingsEx.getCause().getCause().getMessage());
                    reportDataEntities.add(exceptionReportDataEntity);
                    reportEntity.setFileName(xmlFileName);
                    reportEntity.setCreatedDate(new Date());
                    reportEntity.setType(RecapConstants.FAILURE);
                    reportEntity.setInstitutionName(institutionName);
                }
            }
            bibliographicEntity.setHoldingsEntities(savedHoldingsEntities);
            bibliographicEntity.setItemEntities(savedItemEntities);
            bibliographicDetailsRepository.save(bibliographicEntity);
        } catch (Exception bibEx) {
            logger.error(RecapConstants.ERROR,bibEx);
            etlDataLoadDAOService.clearSession();
            List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibFailureReportEntity(bibliographicEntity);

            ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
            exceptionReportDataEntity.setHeaderName(RecapConstants.EXCEPTION_MESSAGE);

            if(bibEx.getCause() != null && bibEx.getCause().getCause() != null) {
                exceptionReportDataEntity.setHeaderValue(bibEx.getCause().getCause().getMessage());
            } else {
                exceptionReportDataEntity.setHeaderValue(bibEx.getMessage());
            }
            reportDataEntities.add(exceptionReportDataEntity);
            reportEntity.setFileName(xmlFileName);
            reportEntity.setCreatedDate(new Date());
            reportEntity.setType(RecapConstants.FAILURE);
            reportEntity.setInstitutionName(institutionName);
        }
        return reportEntity;
    }

    private void setItemFailureReportInfo(DBReportUtil dbReportUtil, BibliographicEntity bibliographicEntity, ReportEntity reportEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity,
                                          Exception itemEx,String failureMessage) {
        List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsAndItemsFailureReportEntities(bibliographicEntity, holdingsEntity, itemEntity);
        ReportDataEntity exceptionReportDataEntity = new ReportDataEntity();
        exceptionReportDataEntity.setHeaderName(RecapConstants.EXCEPTION_MESSAGE);
        if (failureMessage!=null) {
            exceptionReportDataEntity.setHeaderValue(failureMessage);
        } else {
            exceptionReportDataEntity.setHeaderValue(itemEx.getCause().getCause().getMessage());
        }
        reportDataEntities.add(exceptionReportDataEntity);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setFileName(xmlFileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(RecapConstants.FAILURE);
        reportEntity.setInstitutionName(institutionName);
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

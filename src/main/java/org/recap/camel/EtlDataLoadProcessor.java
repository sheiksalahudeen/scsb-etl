package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rajeshbabuk on 18/7/16.
 */
@Component
public class EtlDataLoadProcessor {

    Logger logger = LoggerFactory.getLogger(EtlDataLoadProcessor.class);

    private Integer batchSize;
    private String fileName;
    private String institutionName;

    private RecordProcessor recordProcessor;

    ProducerTemplate producer;

    private XmlRecordRepository xmlRecordRepository;
    private ItemDetailsRepository itemDetailsRepository;
    private HoldingsDetailsRepository holdingsDetailsRepository;
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    public void startLoadProcess() {
        List distinctFileNames = xmlRecordRepository.findDistinctFileNames();
        for (Iterator iterator = distinctFileNames.iterator(); iterator.hasNext(); ) {
            String distinctFileName = (String) iterator.next();
            if (distinctFileName.contains(fileName)) {
                long oldBibsCount = bibliographicDetailsRepository.count();
                long oldHoldingsCount = holdingsDetailsRepository.count();
                long oldItemsCount = itemDetailsRepository.count();
                long oldBibHoldingsCount = bibliographicDetailsRepository.findCountOfBibliographicHoldings();
                long oldBibItemsCount = itemDetailsRepository.findCountOfBibliographicItems();
                long totalDocCount;

                totalDocCount = xmlRecordRepository.countByXmlFileNameContaining(distinctFileName);

                if (totalDocCount > 0) {
                    int quotient = Integer.valueOf(Long.toString(totalDocCount)) / (batchSize);
                    int remainder = Integer.valueOf(Long.toString(totalDocCount)) % (batchSize);

                    int loopCount = remainder == 0 ? quotient : quotient + 1;

                    Page<XmlRecordEntity> xmlRecordEntities = null;
                    long totalStartTime = System.currentTimeMillis();
                    for (int i = 0; i < loopCount; i++) {
                        long startTime = System.currentTimeMillis();
                        xmlRecordEntities = xmlRecordRepository.findByXmlFileName(new PageRequest(i, batchSize), distinctFileName);
                        recordProcessor.setXmlFileName(distinctFileName);
                        recordProcessor.setInstitutionName(institutionName);
                        recordProcessor.process(xmlRecordEntities);
                        long endTime = System.currentTimeMillis();
                        logger.info("Time taken to save: " + xmlRecordEntities.getNumberOfElements() + " bibs and related data is: " + (endTime - startTime) / 1000 + " seconds.");
                    }


                    long totalEndTime = System.currentTimeMillis();
                    logger.info("Total time taken to save: " + xmlRecordEntities.getTotalElements() + " bibs and related data is: " + (totalEndTime - totalStartTime) / 1000 + " seconds.");
                } else {
                    logger.info("No records found to load into DB");
                }

                generateSuccessReport(oldBibsCount, oldHoldingsCount, oldItemsCount, distinctFileName, oldBibHoldingsCount, oldBibItemsCount);
            }
        }
        recordProcessor.shutdownExecutorService();
    }

    private void generateSuccessReport(long oldBibsCount, long oldHoldingsCount, long oldItemsCount, String fileName, long oldBibHoldingsCount, long oldBibItemsCount) {
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        long newBibsCount = bibliographicDetailsRepository.count();
        long newHoldingsCount = holdingsDetailsRepository.count();
        long newItemsCount = itemDetailsRepository.count();
        long newBibHoldingsCount = bibliographicDetailsRepository.findCountOfBibliographicHoldings();
        long newBibItemsCount = itemDetailsRepository.findCountOfBibliographicItems();

        Integer processedBibsCount = Integer.valueOf(new Long(newBibsCount).toString()) - Integer.valueOf(new Long(oldBibsCount).toString());
        Integer processedHoldingsCount = Integer.valueOf(new Long(newHoldingsCount).toString()) - Integer.valueOf(new Long(oldHoldingsCount).toString());
        Integer processedItemsCount = Integer.valueOf(new Long(newItemsCount).toString()) - Integer.valueOf(new Long(oldItemsCount).toString());
        Integer processedBibHoldingsCount = Integer.valueOf(new Long(newBibHoldingsCount).toString()) - Integer.valueOf(new Long(oldBibHoldingsCount).toString());
        Integer processedBibItemsCount = Integer.valueOf(new Long(newBibItemsCount).toString()) - Integer.valueOf(new Long(oldBibItemsCount).toString());
        Integer totalRecordsInfile = Integer.valueOf(new Long(xmlRecordRepository.countByXmlFileName(fileName)).toString());

        ReportDataEntity totalRecordsInFileEntity = new ReportDataEntity();
        totalRecordsInFileEntity.setHeaderName(ReCAPConstants.TOTAL_RECORDS_IN_FILE);
        totalRecordsInFileEntity.setHeaderValue(String.valueOf(totalRecordsInfile));
        reportDataEntities.add(totalRecordsInFileEntity);

        ReportDataEntity totalBibsLoadedEntity = new ReportDataEntity();
        totalBibsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIBS_LOADED);
        totalBibsLoadedEntity.setHeaderValue(String.valueOf(processedBibsCount));
        reportDataEntities.add(totalBibsLoadedEntity);

        ReportDataEntity totalHoldingsLoadedEntity = new ReportDataEntity();
        totalHoldingsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_HOLDINGS_LOADED);
        totalHoldingsLoadedEntity.setHeaderValue(String.valueOf(processedHoldingsCount));
        reportDataEntities.add(totalHoldingsLoadedEntity);

        ReportDataEntity totalItemsLoadedEntity = new ReportDataEntity();
        totalItemsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_ITEMS_LOADED);
        totalItemsLoadedEntity.setHeaderValue(String.valueOf(processedItemsCount));
        reportDataEntities.add(totalItemsLoadedEntity);

        ReportDataEntity totalBibHoldingsLoadedEntity = new ReportDataEntity();
        totalBibHoldingsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIB_HOLDINGS_LOADED);
        totalBibHoldingsLoadedEntity.setHeaderValue(String.valueOf(processedBibHoldingsCount));
        reportDataEntities.add(totalBibHoldingsLoadedEntity);

        ReportDataEntity totalBiBItemsLoadedEntity = new ReportDataEntity();
        totalBiBItemsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIB_ITEMS_LOADED);
        totalBiBItemsLoadedEntity.setHeaderValue(String.valueOf(processedBibItemsCount));
        reportDataEntities.add(totalBiBItemsLoadedEntity);

        ReportDataEntity fileNameEntity = new ReportDataEntity();
        fileNameEntity.setHeaderName(ReCAPConstants.FILE_NAME);
        fileNameEntity.setHeaderValue(fileName);
        reportDataEntities.add(fileNameEntity);

        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(org.recap.ReCAPConstants.SUCCESS);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setInstitutionName(institutionName);

        producer.sendBody(ReCAPConstants.REPORT_Q, reportEntity);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ProducerTemplate getProducer() {
        return producer;
    }

    public void setProducer(ProducerTemplate producer) {
        this.producer = producer;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public void setItemDetailsRepository(ItemDetailsRepository itemDetailsRepository) {
        this.itemDetailsRepository = itemDetailsRepository;
    }

    public HoldingsDetailsRepository getHoldingsDetailsRepository() {
        return holdingsDetailsRepository;
    }

    public void setHoldingsDetailsRepository(HoldingsDetailsRepository holdingsDetailsRepository) {
        this.holdingsDetailsRepository = holdingsDetailsRepository;
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public XmlRecordRepository getXmlRecordRepository() {
        return xmlRecordRepository;
    }

    public void setXmlRecordRepository(XmlRecordRepository xmlRecordRepository) {
        this.xmlRecordRepository = xmlRecordRepository;
    }

    public RecordProcessor getRecordProcessor() {
        return recordProcessor;
    }

    public void setRecordProcessor(RecordProcessor recordProcessor) {
        this.recordProcessor = recordProcessor;
    }
}

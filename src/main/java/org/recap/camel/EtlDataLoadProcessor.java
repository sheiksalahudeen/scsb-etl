package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.recap.RecapConstants;
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

    private static final Logger logger = LoggerFactory.getLogger(EtlDataLoadProcessor.class);

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
        long totalStartTime = System.currentTimeMillis();
        for (Iterator iterator = distinctFileNames.iterator(); iterator.hasNext(); ) {
            String distinctFileName = (String) iterator.next();
            if (distinctFileName.contains(fileName)) {
                Integer instIdByFileName = xmlRecordRepository.findInstIdByFileNames(distinctFileName);
                long oldBibsCount = bibliographicDetailsRepository.countByOwningInstitutionId(instIdByFileName);
                long oldHoldingsCount = holdingsDetailsRepository.countByOwningInstitutionId(instIdByFileName);
                long oldItemsCount = itemDetailsRepository.countByOwningInstitutionId(instIdByFileName);
                long oldBibHoldingsCount = bibliographicDetailsRepository.findCountOfBibliographicHoldingsByInstId(instIdByFileName);
                long oldBibItemsCount = itemDetailsRepository.findCountOfBibliographicItemsByInstId(instIdByFileName);
                long totalDocCount;

                totalDocCount = xmlRecordRepository.countByXmlFileName(distinctFileName);

                if (totalDocCount > 0) {
                    int quotient = Integer.valueOf(Long.toString(totalDocCount)) / (batchSize);
                    int remainder = Integer.valueOf(Long.toString(totalDocCount)) % (batchSize);

                    int loopCount = remainder == 0 ? quotient : quotient + 1;

                    Page<XmlRecordEntity> xmlRecordEntities = null;
                    for (int iteration = 0; iteration < loopCount; iteration++) {
                        long startTime = System.currentTimeMillis();
                        xmlRecordEntities = xmlRecordRepository.findByXmlFileName(new PageRequest(iteration, batchSize), distinctFileName);
                        recordProcessor.setXmlFileName(distinctFileName);
                        recordProcessor.setInstitutionName(institutionName);
                        recordProcessor.process(xmlRecordEntities);
                        long endTime = System.currentTimeMillis();
                        logger.info("File name : {} , Total Docs : {}, Total Loops : {}, Current Iteration : {} , Time taken to save: {} bibs and related data is: {} seconds." ,
                                distinctFileName, totalDocCount, loopCount, iteration, xmlRecordEntities.getNumberOfElements() , (endTime - startTime) / 1000 );
                    }


                    long totalEndTime = System.currentTimeMillis();
                    logger.info("File name : {} , Total time taken to save: {} bibs and related data is: {} seconds." ,distinctFileName, xmlRecordEntities.getTotalElements() , (totalEndTime - totalStartTime) / 1000);
                } else {
                    logger.info("No records found to load into DB");
                }

                generateSuccessReport(oldBibsCount, oldHoldingsCount, oldItemsCount, distinctFileName, oldBibHoldingsCount, oldBibItemsCount, instIdByFileName);
            }
        }
        recordProcessor.shutdownExecutorService();
    }

    private void generateSuccessReport(long oldBibsCount, long oldHoldingsCount, long oldItemsCount, String fileName, long oldBibHoldingsCount, long oldBibItemsCount, Integer instIdByFileName) {
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        long newBibsCount = bibliographicDetailsRepository.countByOwningInstitutionId(instIdByFileName);
        long newHoldingsCount = holdingsDetailsRepository.countByOwningInstitutionId(instIdByFileName);
        long newItemsCount = itemDetailsRepository.countByOwningInstitutionId(instIdByFileName);
        long newBibHoldingsCount = bibliographicDetailsRepository.findCountOfBibliographicHoldingsByInstId(instIdByFileName);
        long newBibItemsCount = itemDetailsRepository.findCountOfBibliographicItemsByInstId(instIdByFileName);
        Integer processedBibsCount = Integer.valueOf(Long.toString(newBibsCount)) - Integer.valueOf(Long.toString(oldBibsCount));
        Integer processedHoldingsCount = Integer.valueOf(Long.toString(newHoldingsCount)) - Integer.valueOf(Long.toString(oldHoldingsCount));
        Integer processedItemsCount = Integer.valueOf(Long.toString(newItemsCount)) - Integer.valueOf(Long.toString(oldItemsCount));
        Integer processedBibHoldingsCount = Integer.valueOf(Long.toString(newBibHoldingsCount)) - Integer.valueOf(Long.toString(oldBibHoldingsCount));
        Integer processedBibItemsCount = Integer.valueOf(Long.toString(newBibItemsCount)) - Integer.valueOf(Long.toString(oldBibItemsCount));
        Integer totalRecordsInfile = Integer.valueOf(Long.toString(xmlRecordRepository.countByXmlFileName(fileName)));

        ReportDataEntity totalRecordsInFileEntity = new ReportDataEntity();
        totalRecordsInFileEntity.setHeaderName(RecapConstants.TOTAL_RECORDS_IN_FILE);
        totalRecordsInFileEntity.setHeaderValue(String.valueOf(totalRecordsInfile));
        reportDataEntities.add(totalRecordsInFileEntity);

        ReportDataEntity totalBibsLoadedEntity = new ReportDataEntity();
        totalBibsLoadedEntity.setHeaderName(RecapConstants.TOTAL_BIBS_LOADED);
        totalBibsLoadedEntity.setHeaderValue(String.valueOf(processedBibsCount));
        reportDataEntities.add(totalBibsLoadedEntity);

        ReportDataEntity totalHoldingsLoadedEntity = new ReportDataEntity();
        totalHoldingsLoadedEntity.setHeaderName(RecapConstants.TOTAL_HOLDINGS_LOADED);
        totalHoldingsLoadedEntity.setHeaderValue(String.valueOf(processedHoldingsCount));
        reportDataEntities.add(totalHoldingsLoadedEntity);

        ReportDataEntity totalItemsLoadedEntity = new ReportDataEntity();
        totalItemsLoadedEntity.setHeaderName(RecapConstants.TOTAL_ITEMS_LOADED);
        totalItemsLoadedEntity.setHeaderValue(String.valueOf(processedItemsCount));
        reportDataEntities.add(totalItemsLoadedEntity);

        ReportDataEntity totalBibHoldingsLoadedEntity = new ReportDataEntity();
        totalBibHoldingsLoadedEntity.setHeaderName(RecapConstants.TOTAL_BIB_HOLDINGS_LOADED);
        totalBibHoldingsLoadedEntity.setHeaderValue(String.valueOf(processedBibHoldingsCount));
        reportDataEntities.add(totalBibHoldingsLoadedEntity);

        ReportDataEntity totalBiBItemsLoadedEntity = new ReportDataEntity();
        totalBiBItemsLoadedEntity.setHeaderName(RecapConstants.TOTAL_BIB_ITEMS_LOADED);
        totalBiBItemsLoadedEntity.setHeaderValue(String.valueOf(processedBibItemsCount));
        reportDataEntities.add(totalBiBItemsLoadedEntity);

        ReportDataEntity fileNameEntity = new ReportDataEntity();
        fileNameEntity.setHeaderName(RecapConstants.FILE_NAME);
        fileNameEntity.setHeaderValue(fileName);
        reportDataEntities.add(fileNameEntity);

        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(RecapConstants.SUCCESS);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setInstitutionName(institutionName);

        producer.sendBody(RecapConstants.REPORT_Q, reportEntity);
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

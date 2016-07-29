package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

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
                        recordProcessor.process(xmlRecordEntities);
                        long endTime = System.currentTimeMillis();
                        logger.info("Time taken to save: " + xmlRecordEntities.getNumberOfElements() + " bibs and related data is: " + (endTime - startTime) / 1000 + " seconds.");
                    }


                    long totalEndTime = System.currentTimeMillis();
                    logger.info("Total time taken to save: " + xmlRecordEntities.getTotalElements() + " bibs and related data is: " + (totalEndTime - totalStartTime) / 1000 + " seconds.");
                } else {
                    logger.info("No records found to load into DB");
                }

                generateSuccessReport(oldBibsCount, oldHoldingsCount, oldItemsCount, fileName);
            }
        }
        recordProcessor.shutdownExecutorService();
    }

    private void generateSuccessReport(long oldBibsCount, long oldHoldingsCount, long oldItemsCount, String fileName) {
        SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = new SuccessReportReCAPCSVRecord();
        long newBibsCount = bibliographicDetailsRepository.count();
        long newHoldingsCount = holdingsDetailsRepository.count();
        long newItemsCount = itemDetailsRepository.count();
        long newBibHoldingsCount = bibliographicDetailsRepository.findCountOfBibliogrpahicHoldings();
        long newBibItemsCount = itemDetailsRepository.findCountOfBibliogrpahicItems();

        Integer processedBibsCount = Integer.valueOf(new Long(newBibsCount).toString()) - Integer.valueOf(new Long(oldBibsCount).toString());
        Integer processedHoldingsCount = Integer.valueOf(new Long(newHoldingsCount).toString()) - Integer.valueOf(new Long(oldHoldingsCount).toString());
        Integer processedItemsCount = Integer.valueOf(new Long(newItemsCount).toString()) - Integer.valueOf(new Long(oldItemsCount).toString());
        Integer totalRecordsInfile = Integer.valueOf(new Long(xmlRecordRepository.countByXmlFileName(fileName)).toString());
        successReportReCAPCSVRecord.setFileName(fileName);
        successReportReCAPCSVRecord.setTotalRecordsInFile(totalRecordsInfile);
        successReportReCAPCSVRecord.setTotalBibsLoaded(processedBibsCount);
        successReportReCAPCSVRecord.setTotalHoldingsLoaded(processedHoldingsCount);
        successReportReCAPCSVRecord.setTotalItemsLoaded(processedItemsCount);
        successReportReCAPCSVRecord.setTotalBibHoldingsLoaded(newBibHoldingsCount);
        successReportReCAPCSVRecord.setTotalBibItemsLoaded(newBibItemsCount);
        producer.sendBody("seda:etlSuccessReportQ", successReportReCAPCSVRecord);
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

package org.recap.route;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.XmlRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Created by rajeshbabuk on 18/7/16.
 */
@Component
public class EtlDataLoadProcessor {

    Logger logger = LoggerFactory.getLogger(EtlDataLoadProcessor.class);

    private Integer batchSize;
    private String fileName;
    private XmlRecordRepository xmlRecordRepository;
    private RecordProcessor recordProcessor;

    public void startLoadProcess() {
        long totalDocCount;
        if(StringUtils.isNotBlank(fileName)) {
            totalDocCount = xmlRecordRepository.countByXmlFileNameContaining(fileName);
        } else {
            totalDocCount = xmlRecordRepository.count();
        }

        if(totalDocCount > 0) {
            int quotient = Integer.valueOf(Long.toString(totalDocCount)) / (batchSize);
            int remainder = Integer.valueOf(Long.toString(totalDocCount)) % (batchSize);

            int loopCount = remainder == 0 ? quotient : quotient + 1;

            Page<XmlRecordEntity> xmlRecordEntities = null;
            long totalStartTime = System.currentTimeMillis();
            for (int i = 0; i < loopCount; i++) {
                long startTime = System.currentTimeMillis();
                if (StringUtils.isNotBlank(fileName)) {
                    xmlRecordEntities = xmlRecordRepository.findByXmlFileNameContaining(new PageRequest(i, batchSize), fileName);
                } else {
                    xmlRecordEntities = xmlRecordRepository.findAll(new PageRequest(i, batchSize));
                }
                recordProcessor.process(xmlRecordEntities);
                long endTime = System.currentTimeMillis();
                logger.info("Time taken to save: " + xmlRecordEntities.getNumberOfElements() + " bib and related data is: " + (endTime - startTime) / 1000 + " seconds.");
            }

            recordProcessor.cleanUp();
            long totalEndTime = System.currentTimeMillis();
            logger.info("Time taken to save: " + xmlRecordEntities.getTotalElements() + " bib and related data is: " + (totalEndTime - totalStartTime) / 1000 + " seconds.");
        } else {
            logger.info("No records found to load into DB");
        }
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

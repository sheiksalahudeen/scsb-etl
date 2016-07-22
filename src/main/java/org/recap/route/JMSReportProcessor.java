package org.recap.route;

import org.recap.model.etl.LoadReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
@Component
public class JMSReportProcessor {

    Logger logger = LoggerFactory.getLogger(JMSReportProcessor.class);


    public void processReport(List<LoadReportEntity> loadReportEntities) {
        long startTime = System.currentTimeMillis();
        try {
//            csvUtil.writeLoadReportToCsv(loadReportEntities);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time taken to generate report for " + loadReportEntities.size() + " : " + (endTime - startTime) / 1000 + " seconds.");
    }
}

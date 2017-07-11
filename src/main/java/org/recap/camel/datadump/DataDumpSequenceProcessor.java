package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.RecapConstants;
import org.recap.service.executor.datadump.DataDumpSchedulerExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by rajeshbabuk on 7/7/17.
 */
@Component
public class DataDumpSequenceProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpSequenceProcessor.class);

    @Autowired
    private DataDumpSchedulerExecutorService dataDumpSchedulerExecutorService;

    /**
     * This method is invoked by route when data export completion message is available and initiates the other institutions data export in sequence.
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        String message = (String) exchange.getIn().getBody();
        String fetchTypeString = RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION.contains(RecapConstants.INCREMENTAL) ? RecapConstants.INCREMENTAL : RecapConstants.DELETED;
        logger.info("Completed {} export for {} institution", fetchTypeString, message);
        if (RecapConstants.PRINCETON.equals(message)) {
            if (RecapConstants.EXPORT_INCREMENTAL_PUL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = RecapConstants.EXPORT_DELETED_PUL;
                dataDumpSchedulerExecutorService.initiateDataDumpForScheduler(RecapConstants.EXPORT_DATE_SCHEDULER, RecapConstants.PRINCETON, RecapConstants.DATADUMP_FETCHTYPE_DELETED);
            } else if (RecapConstants.EXPORT_DELETED_PUL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = RecapConstants.EXPORT_INCREMENTAL_CUL;
                dataDumpSchedulerExecutorService.initiateDataDumpForScheduler(RecapConstants.EXPORT_DATE_SCHEDULER, RecapConstants.COLUMBIA, null);
            }
        } else if (RecapConstants.COLUMBIA.equals(message)) {
            if (RecapConstants.EXPORT_INCREMENTAL_CUL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = RecapConstants.EXPORT_DELETED_CUL;
                dataDumpSchedulerExecutorService.initiateDataDumpForScheduler(RecapConstants.EXPORT_DATE_SCHEDULER, RecapConstants.COLUMBIA, RecapConstants.DATADUMP_FETCHTYPE_DELETED);
            } else if (RecapConstants.EXPORT_DELETED_CUL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = RecapConstants.EXPORT_INCREMENTAL_NYPL;
                dataDumpSchedulerExecutorService.initiateDataDumpForScheduler(RecapConstants.EXPORT_DATE_SCHEDULER, RecapConstants.NYPL, null);
            }
        } else if (RecapConstants.NYPL.equals(message)) {
            if (RecapConstants.EXPORT_INCREMENTAL_NYPL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = RecapConstants.EXPORT_DELETED_NYPL;
                dataDumpSchedulerExecutorService.initiateDataDumpForScheduler(RecapConstants.EXPORT_DATE_SCHEDULER, RecapConstants.NYPL, RecapConstants.DATADUMP_FETCHTYPE_DELETED);
            } else if (RecapConstants.EXPORT_DELETED_NYPL.equalsIgnoreCase(RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION)) {
                RecapConstants.EXPORT_SCHEDULER_CALL = false;
                RecapConstants.EXPORT_DATE_SCHEDULER = "";
                RecapConstants.EXPORT_FETCH_TYPE_INSTITUTION = "";
            }
        }
    }
}

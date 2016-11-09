package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.recap.model.export.DeletedRecord;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;

import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

public class DeletedJsonFormatActiveMQConsumer {

    DeletedJsonFormatterService deletedJsonFormatterService;

    public DeletedJsonFormatActiveMQConsumer(DeletedJsonFormatterService deletedJsonFormatterService) {
        this.deletedJsonFormatterService = deletedJsonFormatterService;
    }

    public String processDeleteJsonString(Exchange exchange) throws Exception {
        List<DeletedRecord> deletedRecordList = (List<DeletedRecord>) exchange.getIn().getBody();
        System.out.println("Num records to generate json for: " + deletedRecordList.size());
        long startTime = System.currentTimeMillis();

        String formattedOutputForDeletedRecords = deletedJsonFormatterService.getJsonForDeletedRecords(deletedRecordList);
        String deletedJsonString = formattedOutputForDeletedRecords.format(formattedOutputForDeletedRecords);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate json for :"  + deletedRecordList.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return deletedJsonString;
    }
}


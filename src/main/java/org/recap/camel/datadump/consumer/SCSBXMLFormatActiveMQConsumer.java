package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.marc4j.marc.Record;
import org.recap.model.jaxb.BibRecord;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;

import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

public class SCSBXMLFormatActiveMQConsumer {

    SCSBXmlFormatterService scsbXmlFormatterService;

    public SCSBXMLFormatActiveMQConsumer(SCSBXmlFormatterService scsbXmlFormatterService) {
        this.scsbXmlFormatterService = scsbXmlFormatterService;
    }

    public String processSCSBXmlString(Exchange exchange) throws Exception {
        List<BibRecord> records = (List<BibRecord>) exchange.getIn().getBody();
        System.out.println("Num records to generate XMl for: " + records.size());
        long startTime = System.currentTimeMillis();

        String toSCSBXmlString = scsbXmlFormatterService.getFormattedOutputForBibRecords(records);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate marc xml for :"  + records.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return toSCSBXmlString;
    }
}


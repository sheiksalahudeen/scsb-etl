package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.marc4j.marc.Record;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;

import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

public class MarcXMLFormatActiveMQConsumer {

    MarcXmlFormatterService marcXmlFormatterService;

    public MarcXMLFormatActiveMQConsumer(MarcXmlFormatterService marcXmlFormatterService) {
        this.marcXmlFormatterService = marcXmlFormatterService;
    }

    public String processMarcXmlString(Exchange exchange) throws Exception {
        List<Record> records = (List<Record>) exchange.getIn().getBody();
        System.out.println("Num records to generate marc XMl for: " + records.size());
        long startTime = System.currentTimeMillis();

        String toMarcXmlString = marcXmlFormatterService.covertToMarcXmlString(records);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate marc xml for :"  + records.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return toMarcXmlString;
    }
}


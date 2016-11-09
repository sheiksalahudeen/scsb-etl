package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.recap.model.jaxb.BibRecord;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.recap.util.XmlFormatter;

import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

public class SCSBXMLFormatActiveMQConsumer {

    SCSBXmlFormatterService scsbXmlFormatterService;
    XmlFormatter xmlFormatter;

    public SCSBXMLFormatActiveMQConsumer(SCSBXmlFormatterService scsbXmlFormatterService, XmlFormatter xmlFormatter) {
        this.scsbXmlFormatterService = scsbXmlFormatterService;
        this.xmlFormatter = xmlFormatter;
    }

    public String processSCSBXmlString(Exchange exchange) throws Exception {
        List<BibRecord> records = (List<BibRecord>) exchange.getIn().getBody();
        System.out.println("Num records to generate scsb XMl for: " + records.size());
        long startTime = System.currentTimeMillis();

        String formattedOutputForBibRecords = scsbXmlFormatterService.getSCSBXmlForBibRecords(records);
        String toSCSBXmlString = xmlFormatter.format(formattedOutputForBibRecords);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate scsb xml for :"  + records.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return toSCSBXmlString;
    }
}


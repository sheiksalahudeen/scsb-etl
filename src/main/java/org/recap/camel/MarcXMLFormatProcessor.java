package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.marc4j.marc.Record;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

@Component
public class MarcXMLFormatProcessor implements Processor {

    @Autowired
    MarcXmlFormatterService marcXmlFormatterService;

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Record> records = (List<Record>) exchange.getIn().getBody();
        System.out.println("Num records to generate XMl for: " + records.size());
        long startTime = System.currentTimeMillis();

        String toMarcXmlString = marcXmlFormatterService.covertToMarcXmlString(records);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate marc xml for :"  + records.size() + " is : " + (endTime-startTime)/1000 + " ms ");
        exchange.getOut().setBody(toMarcXmlString);
    }
}


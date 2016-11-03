package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.marc4j.marc.Record;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by peris on 11/1/16.
 */

@Component
public class MarcRecordFormatProcessor implements Processor {

    @Autowired
    MarcXmlFormatterService marcXmlFormatterService;

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Record> records = new ArrayList<>();
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();

        List<BibliographicEntity> bibliographicEntities = (List<BibliographicEntity>) exchange.getIn().getBody();

        for (Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator(); iterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = iterator.next();
            marcXmlFormatterService.prepareBibEntity(successList, failureList, records, bibliographicEntity);
        }

        exchange.getOut().setBody(records);
    }
}


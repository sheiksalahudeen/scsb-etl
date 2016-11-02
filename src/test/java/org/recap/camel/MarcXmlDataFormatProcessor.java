package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;

import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */
public class MarcXmlDataFormatProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        List<BibliographicEntity> bibliographicEntitys = (List<BibliographicEntity>) body;
        Map results = (Map) new MarcXmlFormatterService().getFormattedOutput(bibliographicEntitys);
        String formattedString = (String) results.get("formattedString");
        if (null!= formattedString) {
            exchange.getOut().setBody(formattedString);
        }
    }
}


package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */

@Component
public class MarcXmlDataFormatProcessor {

    @Autowired
    MarcXmlFormatterService marcXmlFormatterService;

    public String processEntities(List<BibliographicEntity> bibliographicEntities) throws Exception {
        Map results = (Map) marcXmlFormatterService.getFormattedOutput(bibliographicEntities);
        String formattedString = (String) results.get("formattedString");
        return formattedString;
    }
}


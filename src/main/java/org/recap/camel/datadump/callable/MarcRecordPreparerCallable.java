package org.recap.camel.datadump.callable;

import org.marc4j.marc.Record;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class MarcRecordPreparerCallable implements Callable {

    private final MarcXmlFormatterService marcXmlFormatterService;
    List<BibliographicEntity> bibliographicEntities;

    public MarcRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, MarcXmlFormatterService marcXmlFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.marcXmlFormatterService = marcXmlFormatterService;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String, Object> results = marcXmlFormatterService.prepareMarcRecords(bibliographicEntities);
        return results;
    }
}

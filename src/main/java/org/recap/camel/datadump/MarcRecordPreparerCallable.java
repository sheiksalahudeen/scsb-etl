package org.recap.camel.datadump;

import org.marc4j.marc.Record;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    public List<Record> call() throws Exception {
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        List<Record> records = new ArrayList<>();

        marcXmlFormatterService.prepareBibEntities(successList, failureList, records, bibliographicEntities);
        return records;
    }
}

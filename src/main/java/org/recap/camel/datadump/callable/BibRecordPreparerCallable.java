package org.recap.camel.datadump.callable;

import org.marc4j.marc.Record;
import org.recap.model.jaxb.Bib;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class BibRecordPreparerCallable implements Callable {

    private final SCSBXmlFormatterService scsbXmlFormatterService;
    List<BibliographicEntity> bibliographicEntities;

    public BibRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, SCSBXmlFormatterService scsbXmlFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.scsbXmlFormatterService = scsbXmlFormatterService;
    }

    @Override
    public List<BibRecord> call() throws Exception {
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        List<BibRecord> records = new ArrayList<>();

        scsbXmlFormatterService.prepareBibRecords(successList, failureList, records, bibliographicEntities);
        return records;
    }
}

package org.recap.camel.datadump.callable;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class BibRecordPreparerCallable implements Callable {

    private final SCSBXmlFormatterService scsbXmlFormatterService;
    /**
     * The Bibliographic entities.
     */
    List<BibliographicEntity> bibliographicEntities;

    /**
     * Instantiates a new Bib record preparer callable which is used for multithreading.
     *
     * @param bibliographicEntities   the bibliographic entities
     * @param scsbXmlFormatterService the scsb xml formatter service
     */
    public BibRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, SCSBXmlFormatterService scsbXmlFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.scsbXmlFormatterService = scsbXmlFormatterService;
    }

    /**
     * This method is processed by thread to prepare SCSB format bib records using bibliographic entities and are added to a Map.
     *
     * @return Map with success bib records and failure error messages.
     * @throws Exception
     */
    @Override
    public Map<String, Object> call() throws Exception {
        return scsbXmlFormatterService.prepareBibRecords(bibliographicEntities);
    }
}

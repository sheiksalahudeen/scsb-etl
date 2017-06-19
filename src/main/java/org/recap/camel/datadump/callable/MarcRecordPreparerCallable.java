package org.recap.camel.datadump.callable;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class MarcRecordPreparerCallable implements Callable {

    private final MarcXmlFormatterService marcXmlFormatterService;
    /**
     * The Bibliographic entities.
     */
    List<BibliographicEntity> bibliographicEntities;

    /**
     * Instantiates a new Marc record preparer callable which is used for multithreading.
     *
     * @param bibliographicEntities   the bibliographic entities
     * @param marcXmlFormatterService the marc xml formatter service
     */
    public MarcRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, MarcXmlFormatterService marcXmlFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.marcXmlFormatterService = marcXmlFormatterService;
    }

    /**
     * This method is processed by thread to prepare MARC format bib records using bibliographic entities and are added to a Map.
     *
     * @return Map with success marc records and failure error messages.
     * @throws Exception
     */
    @Override
    public Map<String, Object> call() throws Exception {
        return marcXmlFormatterService.prepareMarcRecords(bibliographicEntities);
    }
}

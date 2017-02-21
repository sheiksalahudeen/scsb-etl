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
    List<BibliographicEntity> bibliographicEntities;

    public BibRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, SCSBXmlFormatterService scsbXmlFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.scsbXmlFormatterService = scsbXmlFormatterService;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        return scsbXmlFormatterService.prepareBibRecords(bibliographicEntities);
    }
}

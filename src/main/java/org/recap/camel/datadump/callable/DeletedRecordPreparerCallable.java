package org.recap.camel.datadump.callable;

import org.recap.model.export.DeletedRecord;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class DeletedRecordPreparerCallable implements Callable {

    private final DeletedJsonFormatterService deletedJsonFormatterService;
    List<BibliographicEntity> bibliographicEntities;

    public DeletedRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, DeletedJsonFormatterService deletedJsonFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.deletedJsonFormatterService = deletedJsonFormatterService;
    }

    @Override
    public List<DeletedRecord> call() throws Exception {
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        List<DeletedRecord> deletedRecordList = new ArrayList<>();

        deletedJsonFormatterService.prepareDeletedRecords(successList,failureList,deletedRecordList, bibliographicEntities);
        return deletedRecordList;
    }
}

package org.recap.camel.datadump.callable;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by peris on 11/3/16.
 */
public class DeletedRecordPreparerCallable implements Callable {

    private final DeletedJsonFormatterService deletedJsonFormatterService;
    /**
     * The Bibliographic entities.
     */
    List<BibliographicEntity> bibliographicEntities;

    /**
     * Instantiates a new Deleted record preparer callable which is used for multithreading.
     *
     * @param bibliographicEntities       the bibliographic entities
     * @param deletedJsonFormatterService the deleted json formatter service
     */
    public DeletedRecordPreparerCallable(List<BibliographicEntity> bibliographicEntities, DeletedJsonFormatterService deletedJsonFormatterService) {
        this.bibliographicEntities = bibliographicEntities;
        this.deletedJsonFormatterService = deletedJsonFormatterService;
    }

    /**
     * This method is processed by thread to prepare deleted records using bibliographic entities and are added to a Map.
     *
     * @return Map with success deleted records and failure error messages.
     * @throws Exception
     */
    @Override
    public Map<String, Object> call() throws Exception {
        return deletedJsonFormatterService.prepareDeletedRecords(bibliographicEntities);
    }
}

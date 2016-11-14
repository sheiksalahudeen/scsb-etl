package org.recap.service.formatter.datadump;

import org.codehaus.jackson.map.ObjectMapper;
import org.recap.ReCAPConstants;
import org.recap.model.export.DeletedRecord;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 29/9/16.
 */
@Service
public class DeletedJsonFormatterService implements DataDumpFormatterInterface {

    private Logger logger = LoggerFactory.getLogger(DeletedJsonFormatterService.class);
    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_DELETED_JSON_FORMAT) ? true:false;
    }

    public Map<String, Object> prepareDeletedRecords(List<BibliographicEntity> bibliographicEntityList){
        Map resultsMap = new HashMap();
        List<DeletedRecord> deletedRecords = new ArrayList<>();
        List<BibliographicEntity> failureRecords = new ArrayList<>();
        for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
            try {
                DeletedRecord deletedRecord = new DeletedRecord();
                List<String> itemIds = new ArrayList<>();
                deletedRecord.setBibId(bibliographicEntity.getBibliographicId().toString());
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    itemIds.add(itemEntity.getBarcode().toString());
                }
                deletedRecord.setItemIds(itemIds);
                deletedRecords.add(deletedRecord);
            } catch (Exception e) {
                logger.error(e.getMessage());
                failureRecords.add(bibliographicEntity);
            }
        }

        resultsMap.put(ReCAPConstants.SUCCESS, deletedRecords);
        resultsMap.put(ReCAPConstants.FAILURE, failureRecords);


        return resultsMap;
    }

    public String getJsonForDeletedRecords(List<DeletedRecord> deletedRecordList){
        String formattedString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            formattedString = mapper.writeValueAsString(deletedRecordList);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return formattedString;
    }
}

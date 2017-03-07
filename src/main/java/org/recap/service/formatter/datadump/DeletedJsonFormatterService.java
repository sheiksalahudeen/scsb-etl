package org.recap.service.formatter.datadump;

import org.codehaus.jackson.map.ObjectMapper;
import org.recap.ReCAPConstants;
import org.recap.model.export.DeletedRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 29/9/16.
 */
@Service
public class DeletedJsonFormatterService implements DataDumpFormatterInterface {

    private static final Logger logger = LoggerFactory.getLogger(DeletedJsonFormatterService.class);
    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_DELETED_JSON_FORMAT) ? true:false;
    }

    public Map<String, Object> prepareDeletedRecords(List<BibliographicEntity> bibliographicEntityList){
        Map resultsMap = new HashMap();
        List<DeletedRecord> deletedRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
            try {
                DeletedRecord deletedRecord = new DeletedRecord();
                List<String> itemBarcodes = new ArrayList<>();
                deletedRecord.setBibId(bibliographicEntity.getBibliographicId().toString());
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    itemBarcodes.add(itemEntity.getBarcode());
                }
                deletedRecord.setItemBarcodes(itemBarcodes);
                deletedRecords.add(deletedRecord);
            } catch (Exception e) {
                logger.error(ReCAPConstants.ERROR,e);
                errors.add(String.valueOf(e.getCause()));
            }
        }

        resultsMap.put(ReCAPConstants.SUCCESS, deletedRecords);
        resultsMap.put(ReCAPConstants.FAILURE, errors);


        return resultsMap;
    }

    public String getJsonForDeletedRecords(List<DeletedRecord> deletedRecordList) throws Exception{
        String formattedString;
        ObjectMapper mapper = new ObjectMapper();
        formattedString = mapper.writeValueAsString(deletedRecordList);
        return formattedString;
    }
}

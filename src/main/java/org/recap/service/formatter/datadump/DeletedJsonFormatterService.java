package org.recap.service.formatter.datadump;

import org.codehaus.jackson.map.ObjectMapper;
import org.recap.RecapConstants;
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

    /**
     * Returns true if selected file format is Json for deleted records data dump.
     *
     * @param formatType the format type
     * @return
     */
    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(RecapConstants.DATADUMP_DELETED_JSON_FORMAT) ? true:false;
    }

    /**
     * Prepare a map with deleted records and failures.
     *
     * @param bibliographicEntityList the bibliographic entity list
     * @return the map
     */
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
                logger.error(RecapConstants.ERROR,e);
                errors.add(String.valueOf(e.getCause()));
            }
        }

        resultsMap.put(RecapConstants.SUCCESS, deletedRecords);
        resultsMap.put(RecapConstants.FAILURE, errors);


        return resultsMap;
    }

    /**
     * Converts deleted records list to Json string.
     *
     * @param deletedRecordList the deleted record list
     * @return the json for deleted records
     * @throws Exception the exception
     */
    public String getJsonForDeletedRecords(List<DeletedRecord> deletedRecordList) throws Exception{
        String formattedString;
        ObjectMapper mapper = new ObjectMapper();
        formattedString = mapper.writeValueAsString(deletedRecordList);
        return formattedString;
    }
}

package org.recap.service.formatter.datadump;

import org.codehaus.jackson.map.ObjectMapper;
import org.recap.ReCAPConstants;
import org.recap.model.export.DeletedRecord;
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

    @Override
    public Object getFormattedOutput(List<BibliographicEntity> bibliographicEntityList) {
        Map<String,Object> successAndFailureFormattedList= new HashMap<>();
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        String formattedString = null;
        String formatError = null;
        List<DeletedRecord> deletedRecordList = new ArrayList<>();
        for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
            try {
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    if (itemEntity.getIsDeleted().equals(ReCAPConstants.IS_DELETED)) {
                        DeletedRecord deletedRecord = new DeletedRecord();
                        deletedRecord.setBibId(bibliographicEntity.getBibliographicId().toString());
                        deletedRecord.setItemId(itemEntity.getBarcode().toString());
                        deletedRecordList.add(deletedRecord);
                    }
                }
                successList.add(bibliographicEntity);
            } catch (Exception e) {
                logger.error(e.getMessage());
                failureList.add(bibliographicEntity);
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            formattedString = mapper.writeValueAsString(deletedRecordList);
        } catch (IOException e) {
            logger.error(e.getMessage());
            formatError = e.getMessage();
        }
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_SUCCESSLIST,successList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FAILURELIST,failureList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATTEDSTRING,formattedString);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATERROR,formatError);
        return successAndFailureFormattedList;
    }


}

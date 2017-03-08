package org.recap.service.executor.datadump;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.search.SearchRecordsRequest;
import org.springframework.stereotype.Service;

/**
 * Created by premkb on 27/9/16.
 */
@Service
public class DeletedDataDumpExecutorService extends AbstractDataDumpExecutorService {

    @Override
    public boolean isInterested(String fetchType) {
        return fetchType.equals(RecapConstants.DATADUMP_FETCHTYPE_DELETED) ? true:false;
    }

    @Override
    public void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest) {
        searchRecordsRequest.setDeleted(true);
        if(StringUtils.isNotBlank(dataDumpRequest.getDate())) {
            searchRecordsRequest.setFieldName(RecapConstants.ITEM_LASTUPDATED_DATE);
            searchRecordsRequest.setFieldValue(getFormattedDateString(dataDumpRequest.getDate()));
        }
    }
}

package org.recap.service.executor.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.search.SearchRecordsRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by premkb on 27/9/16.
 */
@Service
@Scope("prototype")
public class IncrementalDataDumpExecutorService extends AbstractDataDumpExecutorService {

    @Override
    public boolean isInterested(String fetchType) {
        return fetchType.equals(ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL) ? true:false;
    }

    @Override
    public void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest) {
        searchRecordsRequest.setFieldName(ReCAPConstants.BIBITEM_LASTUPDATED_DATE);
        searchRecordsRequest.setFieldValue(getFormattedDateString(dataDumpRequest.getDate()));
    }
}
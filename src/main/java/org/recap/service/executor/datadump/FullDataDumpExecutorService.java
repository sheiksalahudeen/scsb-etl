package org.recap.service.executor.datadump;

import org.recap.model.export.DataDumpRequest;
import org.recap.model.search.SearchRecordsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * Created by premkb on 27/9/16.
 */
@Service
@Scope("prototype")
public class FullDataDumpExecutorService extends AbstractDataDumpExecutorService {

    @Value("${datadump.fetchtype.full}")
    private String fetchTypeFull;

    @Override
    public boolean isInterested(String fetchType) {
        return fetchType.equals(fetchTypeFull) ? true:false;
    }

    @Override
    public void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest) {
        //Do nothing
    }
}

package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.export.FullDataDumpCallable;
import org.recap.model.export.ImprovedFullDataDumpCallable;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Created by premkb on 27/9/16.
 */
@Service
@Scope("prototype")
public class FullDataDumpExecutorService extends AbstractDataDumpExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(FullDataDumpExecutorService.class);

    @Override
    public boolean isInterested(String fetchType) {
        return fetchType.equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL) ? true:false;
    }

    @Override
    public void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest) {

    }
}

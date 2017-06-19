package org.recap.model.export;

import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by premkb on 27/9/16.
 */
@Service
@Scope("prototype")
public class FullDataDumpCallable implements Callable {

    private final int pageNum;
    private final int batchSize;
    private final List<String> institutionCodes;
    private final String fetchType;
    private final String date;
    private final DataDumpRequest dataDumpRequest;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    @Autowired
    private ApplicationContext appContext;

    /**
     * Instantiates a new Full data dump callable.
     *
     * @param pageNum                        the page num
     * @param batchSize                      the batch size
     * @param dataDumpRequest                the data dump request
     * @param bibliographicDetailsRepository the bibliographic details repository
     */
    public FullDataDumpCallable(int pageNum, int batchSize, DataDumpRequest dataDumpRequest, BibliographicDetailsRepository bibliographicDetailsRepository){
        this.pageNum = pageNum;
        this.batchSize = batchSize;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.institutionCodes = dataDumpRequest.getInstitutionCodes();
        this.fetchType = dataDumpRequest.getFetchType();
        this.date = dataDumpRequest.getDate();
        this.dataDumpRequest = dataDumpRequest;
    }

    @Override
    public Object call() throws Exception {
        DataDumpCallableHelperService dataDumpCallableHelperService = appContext.getBean(DataDumpCallableHelperService.class);
        return dataDumpCallableHelperService.getFullDataDumpRecords(pageNum,batchSize,dataDumpRequest,bibliographicDetailsRepository);
    }
}

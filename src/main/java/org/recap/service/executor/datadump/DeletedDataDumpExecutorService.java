package org.recap.service.executor.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.export.DeletedDataDumpCallable;
import org.recap.model.export.FullDataDumpCallable;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 27/9/16.
 */
@Service
public class DeletedDataDumpExecutorService extends AbstractDataDumpExecutorService {

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public boolean isInterested(String fetchType) {
        return fetchType.equals(ReCAPConstants.DATADUMP_FETCHTYPE_DELETED) ? true:false;
    }

    @Override
    public Callable getCallable(int pageNum, int batchSize, DataDumpRequest dataDumpRequest, BibliographicDetailsRepository bibliographicDetailsRepository) {
        Callable callable = appContext.getBean(DeletedDataDumpCallable.class,pageNum,batchSize,dataDumpRequest,bibliographicDetailsRepository);
        return callable;
    }

    @Override
    public Long getTotalRecordsCount(DataDumpRequest dataDumpRequest) {
        Date inputDate = DateUtil.getDateFromString(dataDumpRequest.getDate(), ReCAPConstants.DATE_FORMAT_YYYYMMDDHHMM);
        Long totalRecordCount;
        if (dataDumpRequest.getDate()==null) {
            totalRecordCount = bibliographicDetailsRepository.countDeletedRecordsForFullDump(dataDumpRequest.getCollectionGroupIds(), dataDumpRequest.getInstitutionCodes());
        } else {
            totalRecordCount = bibliographicDetailsRepository.countDeletedRecordsForIncremental(dataDumpRequest.getCollectionGroupIds(), dataDumpRequest.getInstitutionCodes(),inputDate);
        }
        return totalRecordCount;
    }
}

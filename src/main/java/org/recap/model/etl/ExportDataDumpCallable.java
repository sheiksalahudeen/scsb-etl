package org.recap.model.etl;

import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.util.DataDumpUtil;
import org.recap.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by premkb on 19/8/16.
 */
public class ExportDataDumpCallable implements Callable {

    private final int pageNum;
    private final int batchSize;
    private final List<String> institutionCodes;
    private final int fetchType;
    private final String date;
    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    private List<BibliographicEntity> bibliographicEntityList;

    public ExportDataDumpCallable(int pageNum, int batchSize, DataDumpRequest dataDumpRequest,BibliographicDetailsRepository bibliographicDetailsRepository){
        this.pageNum = pageNum;
        this.batchSize = batchSize;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.institutionCodes = dataDumpRequest.getInstitutionCodes();
        this.fetchType = dataDumpRequest.getFetchType();
        this.date = dataDumpRequest.getDate();
    }

    @Override
    public Object call() {
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        bibliographicEntityList = getBibliographicEntities(pageNum,batchSize);
        return dataDumpUtil.getBibRecords(bibliographicEntityList);
    }

    private List<BibliographicEntity> getBibliographicEntities(int page, int size) {
        Page<BibliographicEntity> bibliographicEntities;
        List<BibliographicEntity> bibliographicEntityList;
        if(fetchType==0){
            bibliographicEntities = bibliographicDetailsRepository.findAll(new PageRequest(page, size));
            bibliographicEntityList = bibliographicEntities.getContent();
            return bibliographicEntityList;
        }else{
            Date inputDate = DateUtil.getDateFromString(this.date, ReCAPConstants.DATE_FORMAT_MMDDYYY);
            if (this.institutionCodes != null && inputDate == null) {
                bibliographicEntities = bibliographicDetailsRepository.findByInstitutionCodes(new PageRequest(page, size),this.institutionCodes);
                bibliographicEntityList = bibliographicEntities.getContent();
            } else if(this.institutionCodes == null && inputDate != null){
                bibliographicEntities = bibliographicDetailsRepository.findByLastUpdatedDateAfter(new PageRequest(page, size),inputDate);
                bibliographicEntityList = bibliographicEntities.getContent();
            } else{
                bibliographicEntities = bibliographicDetailsRepository.findByInstitutionCodeAndLastUpdatedDate(new PageRequest(page, size),this.institutionCodes,inputDate);
                bibliographicEntityList = bibliographicEntities.getContent();
            }
            return bibliographicEntityList;
        }

    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    public List<BibliographicEntity> getBibliographicEntityList() {
        return bibliographicEntityList;
    }

    public void setBibliographicEntityList(List<BibliographicEntity> bibliographicEntityList) {
        this.bibliographicEntityList = bibliographicEntityList;
    }
}

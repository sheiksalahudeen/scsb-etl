package org.recap.model.etl;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.util.DataDumpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by premkb on 19/8/16.
 */
public class ExportDataDumpCallable implements Callable {

    private final int pageNum;
    private final int batchSize;
    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    private List<BibliographicEntity> bibliographicEntityList;

    public ExportDataDumpCallable(int pageNum, int batchSize,BibliographicDetailsRepository bibliographicDetailsRepository){
        this.pageNum = pageNum;
        this.batchSize = batchSize;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    @Override
    public Object call() {
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        bibliographicEntityList = getBibliographicEntities(pageNum,batchSize);
        return dataDumpUtil.getBibRecords(bibliographicEntityList);
    }

    private List<BibliographicEntity> getBibliographicEntities(int page, int size) {
        Page<BibliographicEntity> bibliographicEntities = bibliographicDetailsRepository.findAll(new PageRequest(page, size));
        List<BibliographicEntity> bibliographicEntityList = bibliographicEntities.getContent();
        return bibliographicEntityList;
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

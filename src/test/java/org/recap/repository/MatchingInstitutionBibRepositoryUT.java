package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.MatchingInstitutionBibViewEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by premkb on 24/1/17.
 */
public class MatchingInstitutionBibRepositoryUT extends BaseTestCase{

    @Autowired
    private MatchingInstitutionBibRepository matchingInstitutionBibIdRepository;

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @PersistenceContext
    private EntityManager entityManager;
    @Test
    public void findByBibIdList(){
        generateMatchinInfo();
        List<String> bibIdList = new ArrayList<>();
        bibIdList.add("100");
        List<MatchingInstitutionBibViewEntity> matchingInstitutionBibViewList = matchingInstitutionBibIdRepository.findByBibIdList(bibIdList);
        assertNotNull(matchingInstitutionBibViewList);
    }

    private void generateMatchinInfo(){
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setRecordNumber(15);
        reportEntity.setFileName("OCLC,ISBN");
        reportEntity.setType("MultiMatch");
        reportEntity.setCreatedDate(new Date());
        reportEntity.setInstitutionName("ALL");
        List<ReportDataEntity> reportDataEntityList = new ArrayList<>();
        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setReportDataId(400);
        reportDataEntity.setHeaderName("BibId");
        reportDataEntity.setHeaderValue("100,11,12");
        reportDataEntity.setRecordNum("50");
        reportDataEntityList.add(reportDataEntity);
        ReportDataEntity reportDataEntity1 = new ReportDataEntity();
        reportDataEntity1.setReportDataId(401);
        reportDataEntity1.setHeaderName("OwningInstitution");
        reportDataEntity1.setHeaderValue("PUL,NYPL,CUL");
        reportDataEntity1.setRecordNum("50");
        reportDataEntityList.add(reportDataEntity1);
        ReportDataEntity reportDataEntity2 = new ReportDataEntity();
        reportDataEntity2.setReportDataId(402);
        reportDataEntity2.setHeaderName("OwningInstitutionBibId");
        reportDataEntity2.setHeaderValue("3214,.b105351386,17980");
        reportDataEntity2.setRecordNum("50");
        reportDataEntityList.add(reportDataEntity2);
        reportEntity.setReportDataEntities(reportDataEntityList);
        ReportEntity savedReportEntity = reportDetailRepository.saveAndFlush(reportEntity);
        entityManager.refresh(savedReportEntity);
    }
}

package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by SheikS on 8/8/2016.
 */
public class ReportDetailRepositoryTest extends BaseTestCase {

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @Test
    public void testSaveReportEntity() {

        ReportEntity savedReportEntity = saveReportEntity();

        assertNotNull(savedReportEntity);
        assertNotNull(savedReportEntity.getRecordNumber());
        List<ReportDataEntity> savedReportDataEntities = savedReportEntity.getReportDataEntities();
        for (Iterator<ReportDataEntity> iterator = savedReportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity savedReportDataEntity = iterator.next();
            System.out.println(savedReportDataEntity.getHeaderName() + " : " + savedReportDataEntity.getHeaderValue());
        }
    }

    @Test
    public void saveAndFindReportEntity() {
        ReportEntity reportEntity = saveReportEntity();

        List<ReportEntity> reportEntities = reportDetailRepository.findByFileName(reportEntity.getFileName());

        assertNotNull(reportEntities);
        ReportEntity reportEntity1 = reportEntities.get(0);
        assertNotNull(reportEntity1);
        assertNotNull(reportEntity1.getReportDataEntities());
    }

    private ReportEntity saveReportEntity() {
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName("test.xml");
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType("Failure");

        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setHeaderName("Barcode");
        reportDataEntity.setHeaderValue("103");
        reportDataEntities.add(reportDataEntity);

        ReportDataEntity reportDataEntity2 = new ReportDataEntity();
        reportDataEntity2.setHeaderName("CallNumber");
        reportDataEntity2.setHeaderValue("X123");
        reportDataEntities.add(reportDataEntity2);

        ReportDataEntity reportDataEntity3 = new ReportDataEntity();
        reportDataEntity3.setHeaderName("ItemId");
        reportDataEntity3.setHeaderValue("10412");
        reportDataEntities.add(reportDataEntity3);

        ReportDataEntity reportDataEntity4 = new ReportDataEntity();
        reportDataEntity4.setHeaderName("Institution");
        reportDataEntity4.setHeaderValue("PUL");
        reportDataEntities.add(reportDataEntity4);

        reportEntity.setReportDataEntities(reportDataEntities);

        return reportDetailRepository.save(reportEntity);
    }

}
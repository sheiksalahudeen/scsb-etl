package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.ReportEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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

        List<ReportEntity> reportEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setHeaderName("Barcode");
        reportEntity.setHeaderValue("103");
        reportEntity.setFileName("sampleFile.xml");
        reportEntity.setRecordNumber(1);
        reportEntities.add(reportEntity);

        ReportEntity reportEntity2 = new ReportEntity();
        reportEntity2.setHeaderName("CallNumber");
        reportEntity2.setHeaderValue("X");
        reportEntity2.setFileName("sampleFile.xml");
        reportEntity2.setRecordNumber(1);
        reportEntities.add(reportEntity2);

        ReportEntity reportEntity3 = new ReportEntity();
        reportEntity3.setHeaderName("Barcode");
        reportEntity3.setHeaderValue("104");
        reportEntity3.setFileName("sampleFile.xml");
        reportEntity3.setRecordNumber(2);
        reportEntities.add(reportEntity3);

        ReportEntity reportEntity4 = new ReportEntity();
        reportEntity4.setHeaderName("CallNumber");
        reportEntity4.setHeaderValue("X");
        reportEntity4.setFileName("sampleFile.xml");
        reportEntity4.setRecordNumber(2);
        reportEntities.add(reportEntity4);

        reportDetailRepository.save(reportEntities);

        System.out.println("test");



    }

}
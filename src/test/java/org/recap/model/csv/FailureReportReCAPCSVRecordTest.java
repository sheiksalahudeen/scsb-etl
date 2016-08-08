package org.recap.model.csv;

import org.junit.Ignore;
import org.junit.Test;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.CSVReportHelperUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by SheikS on 8/8/2016.
 */
public class FailureReportReCAPCSVRecordTest {

    @Ignore
    @Test
    public void testPrepareFailureReportReCAPCSVRecord() {
        List<ReportEntity> reportEntities = new ArrayList<>();
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setHeaderName("itemBarcode");
        reportEntity.setHeaderValue("103");
        reportEntity.setFileName("sampleFile.xml");
        reportEntity.setRecordNumber(1);
        reportEntities.add(reportEntity);

        ReportEntity reportEntity2 = new ReportEntity();
        reportEntity2.setHeaderName("customerCode");
        reportEntity2.setHeaderValue("C101");
        reportEntity2.setFileName("sampleFile.xml");
        reportEntity2.setRecordNumber(1);
        reportEntities.add(reportEntity2);

        ReportEntity reportEntity3 = new ReportEntity();
        reportEntity3.setHeaderName("localItemId");
        reportEntity3.setHeaderValue("L104");
        reportEntity3.setFileName("sampleFile.xml");
        reportEntity3.setRecordNumber(1);
        reportEntities.add(reportEntity3);

        ReportEntity reportEntity4 = new ReportEntity();
        reportEntity4.setHeaderName("title");
        reportEntity4.setHeaderValue("Title");
        reportEntity4.setFileName("sampleFile.xml");
        reportEntity4.setRecordNumber(1);
        reportEntities.add(reportEntity4);

        ReportEntity reportEntity5 = new ReportEntity();
        reportEntity5.setHeaderName("owningInstitution");
        reportEntity5.setHeaderValue("I101");
        reportEntity5.setFileName("sampleFile.xml");
        reportEntity5.setRecordNumber(1);
        reportEntities.add(reportEntity5);

        ReportEntity reportEntity6 = new ReportEntity();
        reportEntity6.setHeaderName("owningInstitutionBibId");
        reportEntity6.setHeaderValue("OWNBIBID-1");
        reportEntity6.setFileName("sampleFile.xml");
        reportEntity6.setRecordNumber(1);
        reportEntities.add(reportEntity6);

        ReportEntity reportEntity7 = new ReportEntity();
        reportEntity7.setHeaderName("owningInstitutionHoldingsId");
        reportEntity7.setHeaderValue("OWNHOLDINGiD-1");
        reportEntity7.setFileName("sampleFile.xml");
        reportEntity7.setRecordNumber(1);
        reportEntities.add(reportEntity7);

        CSVReportHelperUtil csvReportHelperUtil = new CSVReportHelperUtil();

        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = csvReportHelperUtil.prepareFailureReportReCAPCSVRecord(reportEntities);

        assertNotNull(failureReportReCAPCSVRecord);

        System.out.println(failureReportReCAPCSVRecord.toString());


    }

}
package org.recap.report;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by peris on 8/17/16.
 */
public class ReportGenerator_UT extends BaseTestCase {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ReportGenerator reportGenerator;
    private String fileName = "test.xml";

    @Value("${etl.report.directory}")
    private String reportDirectory;


    @Test
    public void generateTest() throws Exception {

        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType("Failure");

        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setHeaderName("ItemBarcode");
        reportDataEntity.setHeaderValue("103");
        reportDataEntities.add(reportDataEntity);

        ReportDataEntity reportDataEntity2 = new ReportDataEntity();
        reportDataEntity2.setHeaderName("CustomerCode");
        reportDataEntity2.setHeaderValue("PA");
        reportDataEntities.add(reportDataEntity2);

        ReportDataEntity reportDataEntity3 = new ReportDataEntity();
        reportDataEntity3.setHeaderName("LocalItemId");
        reportDataEntity3.setHeaderValue("10412");
        reportDataEntities.add(reportDataEntity3);

        ReportDataEntity reportDataEntity4 = new ReportDataEntity();
        reportDataEntity4.setHeaderName("OwningInstitution");
        reportDataEntity4.setHeaderValue("PUL");
        reportDataEntities.add(reportDataEntity4);

        reportEntity.setReportDataEntities(reportDataEntities);

        ReportEntity savedReportEntity = reportDetailRepository.save(reportEntity);

        Calendar cal = Calendar.getInstance();
        Date from = savedReportEntity.getCreatedDate();
        cal.setTime(from);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        from = cal.getTime();
        Date to = savedReportEntity.getCreatedDate();
        cal.setTime(to);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        to = cal.getTime();


        ArrayList<ReportEntity> expectedReportEntities = new ArrayList<>();
        expectedReportEntities.add(reportEntity);

        String generatedReportFileName = reportGenerator.generateReport(fileName, from, to);

        Thread.sleep(1000);

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);
    }

    class CustomArgumentMatcher extends ArgumentMatcher {
        @Override
        public boolean matches(Object argument) {
            return false;
        }
    }


}
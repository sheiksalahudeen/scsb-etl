package org.recap.report;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.recap.BaseTestCase;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by peris on 8/17/16.
 */
public class ReportGenerator_UT extends BaseTestCase {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    private String fileName = "test.xml";

    @Value("${etl.report.directory}")
    private String reportDirectory;

    @Autowired
    ReportGenerator reportGenerator;


    @Test
    public void generateFailureReportTest() throws Exception {

        ReportEntity savedReportEntity1 = saveFailureReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), "FileSystem");

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);
    }

    @Test
    public void generateSuccessReportTest() throws Exception {

        ReportEntity savedReportEntity1 = saveSuccessReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), "FileSystem");

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);
    }

    @Test
    public void generateFailureReportForTwoEntity() throws Exception {
        ReportEntity savedReportEntity1 = saveFailureReportEntity();
        ReportEntity savedReportEntity2 = saveFailureReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), "FileSystem");

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);

    }

    @Test
    public void uploadFailureReportToFTP() throws Exception {
        ReportEntity savedReportEntity = saveFailureReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity.getCreatedDate(), savedReportEntity.getType(), savedReportEntity.getInstitutionName(), "FTP");

        assertNotNull(generatedReportFileName);
    }

    @Test
    public void uploadSuccessReportToFTP() throws Exception {
        ReportEntity savedReportEntity = saveSuccessReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity.getCreatedDate(), savedReportEntity.getType(), savedReportEntity.getInstitutionName(), "FTP");

        assertNotNull(generatedReportFileName);
    }

    private ReportEntity saveFailureReportEntity() {
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType("Failure");
        reportEntity.setInstitutionName("PUL");

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

        return reportDetailRepository.save(reportEntity);
    }

    private ReportEntity saveSuccessReportEntity() {
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportDataEntity totalRecordsInFileEntity = new ReportDataEntity();
        totalRecordsInFileEntity.setHeaderName("TotalRecordsInFile");
        totalRecordsInFileEntity.setHeaderValue(String.valueOf(10000));
        reportDataEntities.add(totalRecordsInFileEntity);

        ReportDataEntity totalBibsLoadedEntity = new ReportDataEntity();
        totalBibsLoadedEntity.setHeaderName("TotalBibsLoaded");
        totalBibsLoadedEntity.setHeaderValue(String.valueOf(10000));
        reportDataEntities.add(totalBibsLoadedEntity);

        ReportDataEntity totalHoldingsLoadedEntity = new ReportDataEntity();
        totalHoldingsLoadedEntity.setHeaderName("TotalHoldingsLoaded");
        totalHoldingsLoadedEntity.setHeaderValue(String.valueOf(8000));
        reportDataEntities.add(totalHoldingsLoadedEntity);

        ReportDataEntity totalItemsLoadedEntity = new ReportDataEntity();
        totalItemsLoadedEntity.setHeaderName("TotalItemsLoaded");
        totalItemsLoadedEntity.setHeaderValue(String.valueOf(12000));
        reportDataEntities.add(totalItemsLoadedEntity);

        ReportDataEntity totalBibHoldingsLoadedEntity = new ReportDataEntity();
        totalBibHoldingsLoadedEntity.setHeaderName("TotalBibHoldingsLoaded");
        totalBibHoldingsLoadedEntity.setHeaderValue(String.valueOf(18000));
        reportDataEntities.add(totalBibHoldingsLoadedEntity);

        ReportDataEntity totalBiBItemsLoadedEntity = new ReportDataEntity();
        totalBiBItemsLoadedEntity.setHeaderName("TotalBibItemsLoaded");
        totalBiBItemsLoadedEntity.setHeaderValue(String.valueOf(22000));
        reportDataEntities.add(totalBiBItemsLoadedEntity);

        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType("Success");
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setInstitutionName("PUL");

        ReportEntity savedReportEntity = reportDetailRepository.save(reportEntity);
        return savedReportEntity;
    }

    private String generateReport(Date createdDate, String reportType, String institutionName, String transmissionType) throws InterruptedException {
        Calendar cal = Calendar.getInstance();
        Date from = createdDate;
        cal.setTime(from);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        from = cal.getTime();
        Date to = createdDate;
        cal.setTime(to);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        to = cal.getTime();

        String generatedFileName = reportGenerator.generateReport(fileName, reportType, institutionName, from, to, transmissionType);

        Thread.sleep(1000);

        return generatedFileName;
    }

    class CustomArgumentMatcher extends ArgumentMatcher {
        @Override
        public boolean matches(Object argument) {
            return false;
        }
    }

}
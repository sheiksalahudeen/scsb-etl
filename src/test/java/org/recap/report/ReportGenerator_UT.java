package org.recap.report;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
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

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), "ETL",savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), org.recap.ReCAPConstants.FILE_SYSTEM);

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);
    }

    @Test
    public void generateSuccessReportTest() throws Exception {

        ReportEntity savedReportEntity1 = saveSuccessReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), "ETL",savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), org.recap.ReCAPConstants.FILE_SYSTEM);

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

        String generatedReportFileName = generateReport(savedReportEntity1.getCreatedDate(), "ETL",savedReportEntity1.getType(), savedReportEntity1.getInstitutionName(), ReCAPConstants.FILE_SYSTEM);

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);

    }

    @Test
    public void uploadFailureReportToFTP() throws Exception {
        ReportEntity savedReportEntity = saveFailureReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity.getCreatedDate(), "ETL",savedReportEntity.getType(), savedReportEntity.getInstitutionName(), ReCAPConstants.FTP);

        assertNotNull(generatedReportFileName);
    }

    @Test
    public void uploadSuccessReportToFTP() throws Exception {
        ReportEntity savedReportEntity = saveSuccessReportEntity();

        String generatedReportFileName = generateReport(savedReportEntity.getCreatedDate(), "ETL",savedReportEntity.getType(), savedReportEntity.getInstitutionName(), ReCAPConstants.FTP);

        assertNotNull(generatedReportFileName);
    }

    @Test
    public void generateReportWithoutFileName() throws Exception {
        ReportEntity savedSuccessReportEntity1 = saveSuccessReportEntity();
        ReportEntity savedSuccessReportEntity2 = saveSuccessReportEntity();
        fileName = "";
        String generatedReportFileName = generateReport(savedSuccessReportEntity1.getCreatedDate(), "ETL",savedSuccessReportEntity1.getType(), savedSuccessReportEntity1.getInstitutionName(), ReCAPConstants.FILE_SYSTEM);

        assertNotNull(generatedReportFileName);

        File directory = new File(reportDirectory);
        assertTrue(directory.isDirectory());

        boolean directoryContains = new File(directory, generatedReportFileName).exists();
        assertTrue(directoryContains);
    }

    private ReportEntity saveFailureReportEntity() {
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
        reportEntity.setInstitutionName("PUL");

        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setHeaderName(ReCAPConstants.ITEM_BARCODE);
        reportDataEntity.setHeaderValue("103");
        reportDataEntities.add(reportDataEntity);

        ReportDataEntity reportDataEntity2 = new ReportDataEntity();
        reportDataEntity2.setHeaderName(ReCAPConstants.CUSTOMER_CODE);
        reportDataEntity2.setHeaderValue("PA");
        reportDataEntities.add(reportDataEntity2);

        ReportDataEntity reportDataEntity3 = new ReportDataEntity();
        reportDataEntity3.setHeaderName(ReCAPConstants.LOCAL_ITEM_ID);
        reportDataEntity3.setHeaderValue("10412");
        reportDataEntities.add(reportDataEntity3);

        ReportDataEntity reportDataEntity4 = new ReportDataEntity();
        reportDataEntity4.setHeaderName(ReCAPConstants.OWNING_INSTITUTION);
        reportDataEntity4.setHeaderValue("PUL");
        reportDataEntities.add(reportDataEntity4);

        reportEntity.setReportDataEntities(reportDataEntities);

        return reportDetailRepository.save(reportEntity);
    }

    private ReportEntity saveSuccessReportEntity() {
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportDataEntity totalRecordsInFileEntity = new ReportDataEntity();
        totalRecordsInFileEntity.setHeaderName(ReCAPConstants.TOTAL_RECORDS_IN_FILE);
        totalRecordsInFileEntity.setHeaderValue(String.valueOf(10000));
        reportDataEntities.add(totalRecordsInFileEntity);

        ReportDataEntity totalBibsLoadedEntity = new ReportDataEntity();
        totalBibsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIBS_LOADED);
        totalBibsLoadedEntity.setHeaderValue(String.valueOf(10000));
        reportDataEntities.add(totalBibsLoadedEntity);

        ReportDataEntity totalHoldingsLoadedEntity = new ReportDataEntity();
        totalHoldingsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_HOLDINGS_LOADED);
        totalHoldingsLoadedEntity.setHeaderValue(String.valueOf(8000));
        reportDataEntities.add(totalHoldingsLoadedEntity);

        ReportDataEntity totalItemsLoadedEntity = new ReportDataEntity();
        totalItemsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_ITEMS_LOADED);
        totalItemsLoadedEntity.setHeaderValue(String.valueOf(12000));
        reportDataEntities.add(totalItemsLoadedEntity);

        ReportDataEntity totalBibHoldingsLoadedEntity = new ReportDataEntity();
        totalBibHoldingsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIB_HOLDINGS_LOADED);
        totalBibHoldingsLoadedEntity.setHeaderValue(String.valueOf(18000));
        reportDataEntities.add(totalBibHoldingsLoadedEntity);

        ReportDataEntity totalBiBItemsLoadedEntity = new ReportDataEntity();
        totalBiBItemsLoadedEntity.setHeaderName(ReCAPConstants.TOTAL_BIB_ITEMS_LOADED);
        totalBiBItemsLoadedEntity.setHeaderValue(String.valueOf(22000));
        reportDataEntities.add(totalBiBItemsLoadedEntity);

        ReportDataEntity fileNameLoadedEntity = new ReportDataEntity();
        fileNameLoadedEntity.setHeaderName(ReCAPConstants.FILE_NAME);
        fileNameLoadedEntity.setHeaderValue(fileName);
        reportDataEntities.add(fileNameLoadedEntity);

        reportEntity.setFileName(fileName);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(org.recap.ReCAPConstants.SUCCESS);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setInstitutionName("PUL");

        ReportEntity savedReportEntity = reportDetailRepository.save(reportEntity);
        return savedReportEntity;
    }

    private String generateReport(Date createdDate, String operationType, String reportType, String institutionName, String transmissionType) throws InterruptedException {
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

        String generatedFileName = reportGenerator.generateReport(fileName, operationType,reportType, institutionName, from, to, transmissionType);

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
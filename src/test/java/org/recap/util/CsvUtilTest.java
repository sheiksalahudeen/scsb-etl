package org.recap.util;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.etl.LoadReportEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Created by chenchulakshmig on 6/7/16.
 */
public class CsvUtilTest extends BaseTestCase {

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;

    @Autowired
    CsvUtil csvUtil;

    @Test
    public void checkCsvCreation() throws Exception {
        LoadReportEntity loadReportEntity = new LoadReportEntity();
        loadReportEntity.setOwningInstitution("NYPL");
        loadReportEntity.setOwningInstitutionBibId("111111");
        loadReportEntity.setOwningInstitutionHoldingsId("22222222");
        loadReportEntity.setLocalItemId("33333333");
        loadReportEntity.setItemBarcode("5555555");
        loadReportEntity.setCustomerCode("Open");
        loadReportEntity.setTitle("title");
        loadReportEntity.setCollectionGroupDesignation("shared");
        loadReportEntity.setCreateDateItem(new Date());
        loadReportEntity.setLastUpdatedDateItem(new Date());
        loadReportEntity.setExceptionMessage("exception");

        loadReportEntity.setErrorDescription("error");

        csvUtil.writeLoadReportToCsv(Arrays.asList(loadReportEntity));

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String fileName = reportDirectoryPath + File.separator + loadReportEntity.getOwningInstitution() + "_" + df.format(new Date()) + ".csv";
        File file = new File(fileName);
        assertTrue(file.exists());
    }

}
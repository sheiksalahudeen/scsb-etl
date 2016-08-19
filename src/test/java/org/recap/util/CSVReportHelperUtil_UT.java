package org.recap.util;

import org.junit.Test;
import org.recap.ReCAPConstants;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by peris on 8/16/16.
 */
public class CSVReportHelperUtil_UT {

    @Test
    public void generateCSVRecord() throws Exception {
        ReCAPCSVFailureRecordGenerator reCAPCSVRecordGenerator = new ReCAPCSVFailureRecordGenerator();

        ReportEntity reportEntity = new ReportEntity();
        List dataEntities = new ArrayList<>();

        ReportDataEntity barcodeReportDataEntity = new ReportDataEntity();
        barcodeReportDataEntity.setHeaderName(ReCAPConstants.ITEM_BARCODE);
        barcodeReportDataEntity.setHeaderValue("1231");
        dataEntities.add(barcodeReportDataEntity);

        ReportDataEntity callNumberReportDataEntity = new ReportDataEntity();
        callNumberReportDataEntity.setHeaderName(ReCAPConstants.LOCAL_ITEM_ID);
        callNumberReportDataEntity.setHeaderValue("1231");
        dataEntities.add(callNumberReportDataEntity);


        reportEntity.setReportDataEntities(dataEntities);

        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord =
                reCAPCSVRecordGenerator.prepareFailureReportReCAPCSVRecord(reportEntity);

        assertNotNull(failureReportReCAPCSVRecord);

    }

}
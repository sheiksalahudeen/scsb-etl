package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.OneToMany;

import java.util.Date;
import java.util.List;

/**
 * Created by peris on 7/21/16.
 */

@CsvRecord(generateHeaderColumns = true, separator = ",", quote = "\"", crlf = "UNIX", skipFirstLine = true)
public class ReCAPCSVRecord {
    @DataField(pos = 1)
    private String owningInstitution;
    @DataField(pos = 2)
    private String owningInstitutionBibId;
    @DataField(pos = 3)
    private String owningInstitutionHoldingsId;
    @DataField(pos = 4)
    private String localItemId;
    @DataField(pos = 5)
    private String itemBarcode;
    @DataField(pos = 6)
    private String customerCode;
    @DataField(pos = 7)
    private String title;
    @DataField(pos = 8)
    private String collectionGroupDesignation;
    @DataField(pos = 9, pattern="MM/dd/yyyy hh:mm:ss a")
    private Date createDateItem;
    @DataField(pos = 10, pattern="MM/dd/yyyy hh:mm:ss a")
    private Date lastUpdatedDateItem;
    @DataField(pos = 11)
    private String exceptionMessage;
    @DataField(pos = 12)
    private String errorDescription;
    @DataField(pos = 13)
    private String fileName;

    @OneToMany(mappedTo = "org.recap.model.csv.FailureReportReCAPCSVRecord")
    List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList;

    public List<FailureReportReCAPCSVRecord> getFailureReportReCAPCSVRecordList() {
        return failureReportReCAPCSVRecordList;
    }

    public void setFailureReportReCAPCSVRecordList(List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList) {
        this.failureReportReCAPCSVRecordList = failureReportReCAPCSVRecordList;
    }
}

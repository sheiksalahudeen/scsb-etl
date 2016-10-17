package org.recap.model.csv;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by peris on 7/21/16.
 */

@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX", skipFirstLine = true)
public class ReCAPCSVFailureRecord implements Serializable{
    @DataField(pos = 1, columnName = "Owning Institution")
    private String owningInstitution;
    @DataField(pos = 2, columnName = "Owning Institution Bib Id")
    private String owningInstitutionBibId;
    @DataField(pos = 3, columnName = "Owning Institution Holdings Id")
    private String owningInstitutionHoldingsId;
    @DataField(pos = 4, columnName = "Local Item Id")
    private String localItemId;
    @DataField(pos = 5, columnName = "Item Barcode")
    private String itemBarcode;
    @DataField(pos = 6, columnName = "Customer Code")
    private String customerCode;
    @DataField(pos = 7, columnName = "Title")
    private String title;
    @DataField(pos = 8, columnName = "Collection Group Designation")
    private String collectionGroupDesignation;
    @DataField(pos = 9, pattern="MM/dd/yyyy hh:mm:ss a", columnName = "Create Date Item")
    private Date createDateItem;
    @DataField(pos = 10, pattern="MM/dd/yyyy hh:mm:ss a", columnName = "Last Updated Date Item")
    private Date lastUpdatedDateItem;
    @DataField(pos = 11, columnName = "Exception Message")
    private String exceptionMessage;
    @DataField(pos = 12, columnName = "Error Description")
    private String errorDescription;

    @Ignore
    private String fileName;

    @Ignore
    private String reportType;

    @Ignore
    private String institutionName;

    @OneToMany(mappedTo = "org.recap.model.csv.FailureReportReCAPCSVRecord")
    List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList;

    public List<FailureReportReCAPCSVRecord> getFailureReportReCAPCSVRecordList() {
        return failureReportReCAPCSVRecordList;
    }

    public void setFailureReportReCAPCSVRecordList(List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList) {
        this.failureReportReCAPCSVRecordList = failureReportReCAPCSVRecordList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}

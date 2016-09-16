package org.recap.model.csv;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.OneToMany;

import java.util.List;

/**
 * Created by angelind on 18/8/16.
 */

@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX", skipFirstLine = true)
public class ReCAPCSVSuccessRecord {

    @DataField(pos = 1, columnName = "File Name")
    private String fileName;
    @DataField(pos = 2, columnName = "Total Records In File")
    private Integer totalRecordsInFile;
    @DataField(pos = 3, columnName = "Total Bibs Loaded")
    private Integer totalBibsLoaded;
    @DataField(pos = 4, columnName = "Total Holdings Loaded")
    private Integer totalHoldingsLoaded;
    @DataField(pos = 5, columnName = "Total Bib-Holdings Loaded")
    private Integer totalBibHoldingsLoaded;
    @DataField(pos = 6, columnName = "Total Items Loaded")
    private Integer totalItemsLoaded;
    @DataField(pos = 7, columnName = "Total Bib-Items Loaded")
    private Integer totalBibItemsLoaded;

    @Ignore
    private String reportFileName;

    @Ignore
    private String reportType;

    @Ignore
    private String institutionName;

    @OneToMany(mappedTo = "org.recap.model.csv.SuccessReportReCAPCSVRecord")
    List<SuccessReportReCAPCSVRecord> successReportReCAPCSVRecordList;

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
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

    public List<SuccessReportReCAPCSVRecord> getSuccessReportReCAPCSVRecordList() {
        return successReportReCAPCSVRecordList;
    }

    public void setSuccessReportReCAPCSVRecordList(List<SuccessReportReCAPCSVRecord> successReportReCAPCSVRecordList) {
        this.successReportReCAPCSVRecordList = successReportReCAPCSVRecordList;
    }
}

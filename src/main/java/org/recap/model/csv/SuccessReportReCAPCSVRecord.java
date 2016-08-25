package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

/**
 * Created by angelind on 22/7/16.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX")
public class SuccessReportReCAPCSVRecord {

    @DataField(pos = 1)
    private String fileName;
    @DataField(pos = 2)
    private String totalRecordsInFile;
    @DataField(pos = 3)
    private String totalBibsLoaded;
    @DataField(pos = 4)
    private String totalHoldingsLoaded;
    @DataField(pos = 5)
    private String totalBibHoldingsLoaded;
    @DataField(pos = 6)
    private String totalItemsLoaded;
    @DataField(pos = 7)
    private String totalBibItemsLoaded;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTotalRecordsInFile() {
        return totalRecordsInFile;
    }

    public void setTotalRecordsInFile(String totalRecordsInFile) {
        this.totalRecordsInFile = totalRecordsInFile;
    }

    public String getTotalBibsLoaded() {
        return totalBibsLoaded;
    }

    public void setTotalBibsLoaded(String totalBibsLoaded) {
        this.totalBibsLoaded = totalBibsLoaded;
    }

    public String getTotalHoldingsLoaded() {
        return totalHoldingsLoaded;
    }

    public void setTotalHoldingsLoaded(String totalHoldingsLoaded) {
        this.totalHoldingsLoaded = totalHoldingsLoaded;
    }

    public String getTotalBibHoldingsLoaded() {
        return totalBibHoldingsLoaded;
    }

    public void setTotalBibHoldingsLoaded(String totalBibHoldingsLoaded) {
        this.totalBibHoldingsLoaded = totalBibHoldingsLoaded;
    }

    public String getTotalItemsLoaded() {
        return totalItemsLoaded;
    }

    public void setTotalItemsLoaded(String totalItemsLoaded) {
        this.totalItemsLoaded = totalItemsLoaded;
    }

    public String getTotalBibItemsLoaded() {
        return totalBibItemsLoaded;
    }

    public void setTotalBibItemsLoaded(String totalBibItemsLoaded) {
        this.totalBibItemsLoaded = totalBibItemsLoaded;
    }
}

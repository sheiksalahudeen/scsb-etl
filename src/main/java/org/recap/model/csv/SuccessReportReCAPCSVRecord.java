package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

/**
 * Created by angelind on 22/7/16.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quote = "\"", crlf = "UNIX")
public class SuccessReportReCAPCSVRecord {

    @DataField(pos = 1)
    private String fileName;
    @DataField(pos = 2)
    private Integer totalRecordsInFile;
    @DataField(pos = 3)
    private Integer totalBibsLoaded;
    @DataField(pos = 4)
    private Integer totalHoldingsLoaded;
    @DataField(pos = 5)
    private Integer totalItemsLoaded;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getTotalRecordsInFile() {
        return totalRecordsInFile;
    }

    public void setTotalRecordsInFile(Integer totalRecordsInFile) {
        this.totalRecordsInFile = totalRecordsInFile;
    }

    public Integer getTotalBibsLoaded() {
        return totalBibsLoaded;
    }

    public void setTotalBibsLoaded(Integer totalBibsLoaded) {
        this.totalBibsLoaded = totalBibsLoaded;
    }

    public Integer getTotalHoldingsLoaded() {
        return totalHoldingsLoaded;
    }

    public void setTotalHoldingsLoaded(Integer totalHoldingsLoaded) {
        this.totalHoldingsLoaded = totalHoldingsLoaded;
    }

    public Integer getTotalItemsLoaded() {
        return totalItemsLoaded;
    }

    public void setTotalItemsLoaded(Integer totalItemsLoaded) {
        this.totalItemsLoaded = totalItemsLoaded;
    }
}

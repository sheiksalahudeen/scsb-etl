package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

/**
 * Created by angelind on 22/7/16.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX")
public class SuccessReportReCAPCSVRecord implements Serializable{

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

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets file name.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets total records in file.
     *
     * @return the total records in file
     */
    public String getTotalRecordsInFile() {
        return totalRecordsInFile;
    }

    /**
     * Sets total records in file.
     *
     * @param totalRecordsInFile the total records in file
     */
    public void setTotalRecordsInFile(String totalRecordsInFile) {
        this.totalRecordsInFile = totalRecordsInFile;
    }

    /**
     * Gets total bibs loaded.
     *
     * @return the total bibs loaded
     */
    public String getTotalBibsLoaded() {
        return totalBibsLoaded;
    }

    /**
     * Sets total bibs loaded.
     *
     * @param totalBibsLoaded the total bibs loaded
     */
    public void setTotalBibsLoaded(String totalBibsLoaded) {
        this.totalBibsLoaded = totalBibsLoaded;
    }

    /**
     * Gets total holdings loaded.
     *
     * @return the total holdings loaded
     */
    public String getTotalHoldingsLoaded() {
        return totalHoldingsLoaded;
    }

    /**
     * Sets total holdings loaded.
     *
     * @param totalHoldingsLoaded the total holdings loaded
     */
    public void setTotalHoldingsLoaded(String totalHoldingsLoaded) {
        this.totalHoldingsLoaded = totalHoldingsLoaded;
    }

    /**
     * Gets total bib holdings loaded.
     *
     * @return the total bib holdings loaded
     */
    public String getTotalBibHoldingsLoaded() {
        return totalBibHoldingsLoaded;
    }

    /**
     * Sets total bib holdings loaded.
     *
     * @param totalBibHoldingsLoaded the total bib holdings loaded
     */
    public void setTotalBibHoldingsLoaded(String totalBibHoldingsLoaded) {
        this.totalBibHoldingsLoaded = totalBibHoldingsLoaded;
    }

    /**
     * Gets total items loaded.
     *
     * @return the total items loaded
     */
    public String getTotalItemsLoaded() {
        return totalItemsLoaded;
    }

    /**
     * Sets total items loaded.
     *
     * @param totalItemsLoaded the total items loaded
     */
    public void setTotalItemsLoaded(String totalItemsLoaded) {
        this.totalItemsLoaded = totalItemsLoaded;
    }

    /**
     * Gets total bib items loaded.
     *
     * @return the total bib items loaded
     */
    public String getTotalBibItemsLoaded() {
        return totalBibItemsLoaded;
    }

    /**
     * Sets total bib items loaded.
     *
     * @param totalBibItemsLoaded the total bib items loaded
     */
    public void setTotalBibItemsLoaded(String totalBibItemsLoaded) {
        this.totalBibItemsLoaded = totalBibItemsLoaded;
    }
}

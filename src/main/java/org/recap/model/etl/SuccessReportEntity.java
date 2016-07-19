package org.recap.model.etl;

import java.io.Serializable;

/**
 * Created by angelind on 19/7/16.
 */
public class SuccessReportEntity implements Serializable {

    private String fileName;
    private Integer totalRecordsInFile;
    private Integer totalBibsLoaded;
    private Integer totalHoldingsLoaded;
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

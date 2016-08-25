package org.recap.model.export;

import java.util.List;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRequest {

    private List<String> institutionCodes;
    private Integer fetchType;
    private String date;
    private int noOfThreads;
    private int batchSize;

    public List<String> getInstitutionCodes() {
        return institutionCodes;
    }

    public void setInstitutionCodes(List<String> institutionCodes) {
        this.institutionCodes = institutionCodes;
    }

    public Integer getFetchType() {
        return fetchType;
    }

    public void setFetchType(Integer fetchType) {
        this.fetchType = fetchType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNoOfThreads() {
        return noOfThreads;
    }

    public void setNoOfThreads(int noOfThreads) {
        this.noOfThreads = noOfThreads;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}

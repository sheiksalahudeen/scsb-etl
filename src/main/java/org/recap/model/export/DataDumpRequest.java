package org.recap.model.export;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRequest {

    private String[] institutionIds;
    private Integer fetchType;
    private String date;
    private int noOfThreads = 5;
    private int batchSize = 8000;

    public String[] getInstitutionIds() {
        return institutionIds;
    }

    public void setInstitutionIds(String[] institutionIds) {
        this.institutionIds = institutionIds;
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

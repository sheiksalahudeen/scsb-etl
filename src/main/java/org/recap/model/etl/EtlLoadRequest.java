package org.recap.model.etl;

import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * Created by rajeshbabuk on 22/6/16.
 */
public class EtlLoadRequest {

    private String fileName;
    private Integer batchSize;
    private MultipartFile file;
    private String userName;
    private String owningInstitutionName;
    private String reportFileName;
    private String reportType;
    private String operationType;
    private String transmissionType;
    private String reportInstitutionName;
    private Date dateFrom;
    private Date dateTo;

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
     * Gets batch size.
     *
     * @return the batch size
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Sets batch size.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Gets file.
     *
     * @return the file
     */
    public MultipartFile getFile() {
        return file;
    }

    /**
     * Sets file.
     *
     * @param file the file
     */
    public void setFile(MultipartFile file) {
        this.file = file;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name.
     *
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets owning institution name.
     *
     * @return the owning institution name
     */
    public String getOwningInstitutionName() {
        return owningInstitutionName;
    }

    /**
     * Sets owning institution name.
     *
     * @param owningInstitutionName the owning institution name
     */
    public void setOwningInstitutionName(String owningInstitutionName) {
        this.owningInstitutionName = owningInstitutionName;
    }

    /**
     * Gets report file name.
     *
     * @return the report file name
     */
    public String getReportFileName() {
        return reportFileName;
    }

    /**
     * Sets report file name.
     *
     * @param reportFileName the report file name
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    /**
     * Gets report type.
     *
     * @return the report type
     */
    public String getReportType() {
        return reportType;
    }

    /**
     * Sets report type.
     *
     * @param reportType the report type
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * Gets operation type.
     *
     * @return the operation type
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * Sets operation type.
     *
     * @param operationType the operation type
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * Gets transmission type.
     *
     * @return the transmission type
     */
    public String getTransmissionType() {
        return transmissionType;
    }

    /**
     * Sets transmission type.
     *
     * @param transmissionType the transmission type
     */
    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    /**
     * Gets report institution name.
     *
     * @return the report institution name
     */
    public String getReportInstitutionName() {
        return reportInstitutionName;
    }

    /**
     * Sets report institution name.
     *
     * @param reportInstitutionName the report institution name
     */
    public void setReportInstitutionName(String reportInstitutionName) {
        this.reportInstitutionName = reportInstitutionName;
    }

    /**
     * Gets date from.
     *
     * @return the date from
     */
    public Date getDateFrom() {
        return dateFrom;
    }

    /**
     * Sets date from.
     *
     * @param dateFrom the date from
     */
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * Gets date to.
     *
     * @return the date to
     */
    public Date getDateTo() {
        return dateTo;
    }

    /**
     * Sets date to.
     *
     * @param dateTo the date to
     */
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}

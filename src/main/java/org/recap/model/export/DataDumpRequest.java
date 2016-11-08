package org.recap.model.export;

import java.util.List;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRequest {

    private List<String> institutionCodes;
    private String fetchType;
    private String date;
    private boolean isRecordsAvailable;
    private List<Integer> collectionGroupIds;
    private String transmissionType;
    private String requestingInstitutionCode;
    private String toEmailAddress;
    private String outputFileFormat;
    private String dateTimeString;

    public List<String> getInstitutionCodes() {
        return institutionCodes;
    }

    public void setInstitutionCodes(List<String> institutionCodes) {
        this.institutionCodes = institutionCodes;
    }

    public String getFetchType() {
        return fetchType;
    }

    public void setFetchType(String fetchType) {
        this.fetchType = fetchType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRecordsAvailable() {
        return isRecordsAvailable;
    }

    public void setRecordsAvailable(boolean recordsAvailable) {
        isRecordsAvailable = recordsAvailable;
    }

    public List<Integer> getCollectionGroupIds() {
        return collectionGroupIds;
    }

    public void setCollectionGroupIds(List<Integer> collectionGroupIds) {
        this.collectionGroupIds = collectionGroupIds;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getRequestingInstitutionCode() {
        return requestingInstitutionCode;
    }

    public void setRequestingInstitutionCode(String requestingInstitutionCode) {
        this.requestingInstitutionCode = requestingInstitutionCode;
    }

    public String getToEmailAddress() {
        return toEmailAddress;
    }

    public void setToEmailAddress(String toEmailAddress) {
        this.toEmailAddress = toEmailAddress;
    }

    public String getOutputFileFormat() {
        return outputFileFormat;
    }

    public void setOutputFileFormat(String outputFileFormat) {
        this.outputFileFormat = outputFileFormat;
    }

    public String getDateTimeString() {
        return dateTimeString;
    }

    public void setDateTimeString(String dateTimeString) {
        this.dateTimeString = dateTimeString;
    }
}

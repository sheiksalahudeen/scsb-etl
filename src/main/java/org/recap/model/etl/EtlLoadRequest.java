package org.recap.model.etl;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by rajeshbabuk on 22/6/16.
 */
public class EtlLoadRequest {

    private String fileName;
    private Integer batchSize;
    private MultipartFile file;
    private String userName;
    private String owningInstitutionName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOwningInstitutionName() {
        return owningInstitutionName;
    }

    public void setOwningInstitutionName(String owningInstitutionName) {
        this.owningInstitutionName = owningInstitutionName;
    }
}

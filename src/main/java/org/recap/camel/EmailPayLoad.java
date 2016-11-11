package org.recap.camel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenchulakshmig on 15/9/16.
 */
public class EmailPayLoad implements Serializable{

    private List<String> institutions;
    private String location;
    private Integer count;
    private Integer failedCount;
    private String to;

    public List<String> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<String> institutions) {
        this.institutions = institutions;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}

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

    /**
     * Gets institutions.
     *
     * @return the institutions
     */
    public List<String> getInstitutions() {
        return institutions;
    }

    /**
     * Sets institutions.
     *
     * @param institutions the institutions
     */
    public void setInstitutions(List<String> institutions) {
        this.institutions = institutions;
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets count.
     *
     * @param count the count
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Gets failed count.
     *
     * @return the failed count
     */
    public Integer getFailedCount() {
        return failedCount;
    }

    /**
     * Sets failed count.
     *
     * @param failedCount the failed count
     */
    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    /**
     * Gets to.
     *
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets to.
     *
     * @param to the to
     */
    public void setTo(String to) {
        this.to = to;
    }
}

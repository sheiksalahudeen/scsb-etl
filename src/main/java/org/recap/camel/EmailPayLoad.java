package org.recap.camel;

import java.util.List;

/**
 * Created by chenchulakshmig on 15/9/16.
 */
public class EmailPayLoad {

    private List<String> institutions;
    private String location;
    private Long count;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}

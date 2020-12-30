package org.apache.dolphinscheduler.microbench.serializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CalvinKirs
 * @date 2020-12-30 16:07
 */

public class TestBean {

    private Map<String, String> items;

    private List<String> ids;

    private Long id;

    private String user;

    private String psd;

    private Double ages;

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPsd() {
        return psd;
    }

    public void setPsd(String psd) {
        this.psd = psd;
    }

    public Double getAges() {
        return ages;
    }

    public void setAges(Double ages) {
        this.ages = ages;
    }
}

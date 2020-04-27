package org.apache.dolphinscheduler.common.task.connectors;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

public class ConnectorsParameters extends AbstractParameters {

    private final String URL_STATUS = "/offset/%s/status" ;
    private final String URL_ADD = "/connectors" ;
    private final String URL_KILL = "/connectors/%s" ;
    private final String URL_ERROR = "/connectors/%s/status" ;



    private String server;

    private String name;

    private String config;

    public String getStatusUrl() {
        return  server + String.format(URL_STATUS,name);
    }
    public String getAddUrl() {
        return  server + URL_ADD ;
    }
    public String getKillUrl() {
        return  server + String.format(URL_KILL,name);
    }
    public String getErrorUrl() {
        return  server + String.format(URL_ERROR,name);
    }

    @Override
    public boolean checkParameters() {
        if (server == null && StringUtils.isNotEmpty(server) ) return false;
        if (name == null && StringUtils.isNotEmpty(name) ) return false;
        if (config == null && StringUtils.isNotEmpty(config) ) return false;
        return true;
    }


    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return null;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

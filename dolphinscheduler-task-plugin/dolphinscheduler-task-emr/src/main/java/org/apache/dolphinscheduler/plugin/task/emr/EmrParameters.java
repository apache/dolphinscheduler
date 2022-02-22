package org.apache.dolphinscheduler.plugin.task.emr;

import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Collections;
import java.util.List;

public class EmrParameters extends AbstractParameters {
    /**
     * the profile name selected in aws credentials file
     */
    private String profileName;
    /**
     * AWS region
     */
    private String region;
    /**
     * job flow define in json format
     */
    private String jobFlowDefineJson;

    @Override
    public boolean checkParameters() {

        return StringUtils.isNotEmpty(profileName)
            && StringUtils.isNotEmpty(region)
            && StringUtils.isNotEmpty(jobFlowDefineJson);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();

    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getJobFlowDefineJson() {
        return jobFlowDefineJson;
    }

    public void setJobFlowDefineJson(String jobFlowDefineJson) {
        this.jobFlowDefineJson = jobFlowDefineJson;
    }

    @Override
    public String toString() {
        return "EmrParameters{" +
            "profileName='" + profileName + '\'' +
            ", region='" + region + '\'' +
            ", jobFlowDefineJson='" + jobFlowDefineJson + '\'' +
            '}';
    }
}

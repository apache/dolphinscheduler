/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.task.emr;


import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Collections;
import java.util.List;

public class EmrParameters extends AbstractParameters {
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

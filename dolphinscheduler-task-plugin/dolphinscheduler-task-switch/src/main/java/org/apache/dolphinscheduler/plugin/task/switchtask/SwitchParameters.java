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

package org.apache.dolphinscheduler.plugin.task.switchtask;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

public class SwitchParameters extends AbstractParameters {

    /**
     * shell script
     */
    private String rawScript;

    /**
     * local parameters
     */
    public List<Property> localParams;

    private SwitchResult switchResult;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    public String getRawScript() {
        return rawScript;
    }

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public List<Property> getLocalParams() {
        return localParams;
    }

    @Override
    public void setLocalParams(List<Property> localParams) {
        this.localParams = localParams;
    }

    public SwitchResult getSwitchResult() {
        return switchResult;
    }

    public void setSwitchResult(SwitchResult switchResult) {
        this.switchResult = switchResult;
    }

    @Override
    public boolean checkParameters() {
        //default next node should not be null
        boolean defaultNode = switchResult != null && switchResult.getNextNode() != null;
        if (!defaultNode) {
            return false;
        }
        //validate conditions must have next node
        List<SwitchCondition> conditions = this.switchResult.getDependTaskList();
        if (conditions != null && conditions.size() != 0) {
            if (conditions.stream().anyMatch(e -> (StringUtils.isNotEmpty(e.getCondition()) && e.getNextNode() == null))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }
}

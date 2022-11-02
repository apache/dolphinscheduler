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

package org.apache.dolphinscheduler.plugin.task.openmldb;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class OpenmldbParameters extends AbstractParameters {

    private String zk;
    private String zkPath;
    private String executeMode;
    /**
     * origin sql script
     */
    private String sql;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    public String getZk() {
        return zk;
    }

    public void setZk(String zk) {
        this.zk = zk;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public String getExecuteMode() {
        return executeMode;
    }

    public void setExecuteMode(String executeMode) {
        this.executeMode = executeMode;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(zk) && StringUtils.isNotEmpty(zkPath) && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return this.resourceList;
    }
}

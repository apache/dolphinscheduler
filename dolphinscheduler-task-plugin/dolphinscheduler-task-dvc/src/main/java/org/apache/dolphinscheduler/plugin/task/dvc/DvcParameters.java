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

package org.apache.dolphinscheduler.plugin.task.dvc;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

public class DvcParameters extends AbstractParameters {

    /**
     * common parameters
     */

    private TaskTypeEnum dvcTaskType;

    private String dvcRepository;

    private String dvcVersion;

    private String dvcDataLocation;

    private String dvcMessage;

    private String dvcLoadSaveDataPath;

    private String dvcStoreUrl;

    public void setDvcTaskType(TaskTypeEnum dvcTaskType) {
        this.dvcTaskType = dvcTaskType;
    }

    public TaskTypeEnum getDvcTaskType() {
        return dvcTaskType;
    }

    public void setDvcRepository(String dvcRepository) {
        this.dvcRepository = dvcRepository;
    }

    public String getDvcRepository() {
        return dvcRepository;
    }

    public void setDvcVersion(String dvcVersion) {
        this.dvcVersion = dvcVersion;
    }

    public String getDvcVersion() {
        return dvcVersion;
    }

    public void setDvcDataLocation(String dvcDataLocation) {
        this.dvcDataLocation = dvcDataLocation;
    }

    public String getDvcDataLocation() {
        return dvcDataLocation;
    }

    public void setDvcMessage(String dvcMessage) {
        this.dvcMessage = dvcMessage;
    }

    public String getDvcMessage() {
        return dvcMessage;
    }

    public void setDvcLoadSaveDataPath(String dvcLoadSaveDataPath) {
        this.dvcLoadSaveDataPath = dvcLoadSaveDataPath;
    }

    public String getDvcLoadSaveDataPath() {
        return dvcLoadSaveDataPath;
    }

    public void setDvcStoreUrl(String dvcStoreUrl) {
        this.dvcStoreUrl = dvcStoreUrl;
    }

    public String getDvcStoreUrl() {
        return dvcStoreUrl;
    }

    @Override
    public boolean checkParameters() {
        Boolean checkResult = true;
        return checkResult;
    }

}


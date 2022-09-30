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

package org.apache.dolphinscheduler.plugin.task.dms;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.databasemigrationservice.model.Tag;

import lombok.Data;

@Data
public class DmsParameters extends AbstractParameters {

    private Boolean isRestartTask = false;
    private Boolean isJsonFormat = false;
    private String jsonData;
    private String replicationTaskIdentifier;
    private String sourceEndpointArn;
    private String targetEndpointArn;
    private String replicationInstanceArn;
    private String migrationType;
    private String tableMappings;
    private String replicationTaskSettings;
    private Date cdcStartTime;
    private String cdcStartPosition;
    private String cdcStopPosition;
    private List<Tag> tags;
    private String taskData;
    private String resourceIdentifier;
    private String replicationTaskArn;
    private String startReplicationTaskType;

    @Override
    public boolean checkParameters() {
        boolean flag;
        if (isJsonFormat) {
            flag = jsonData != null;
        } else if (isRestartTask) {
            flag = replicationTaskArn != null;
        } else {
            flag = sourceEndpointArn != null && targetEndpointArn != null && replicationInstanceArn != null
                && migrationType != null && replicationTaskIdentifier != null && tableMappings != null;
        }
        return flag;
    }

}

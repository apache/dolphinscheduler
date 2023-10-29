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

package org.apache.dolphinscheduler.dao.entity.event;

import org.apache.dolphinscheduler.common.enums.ListenerEventType;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDefinitionDeletedListenerEvent implements AbstractListenerEvent {

    private Integer projectId;
    private Long projectCode;
    private String projectName;
    private String owner;
    private Integer id;
    private Long code;
    private String name;
    private Integer userId;
    private String modifiedBy;
    private Date eventTime;
    @Override
    public ListenerEventType getEventType() {
        return ListenerEventType.PROCESS_DEFINITION_DELETED;
    }

    @Override
    public String getTitle() {
        return String.format("process definition deleted:%s", this.name);
    }
}

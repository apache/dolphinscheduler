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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessStartListenerEvent implements AbstractListenerEvent {

    private Long projectCode;
    private String projectName;
    private String owner;
    private Long processDefinitionCode;
    private String processDefinitionName;
    private Integer processId;
    private String processName;
    private CommandType processType;
    private WorkflowExecutionStatus processState;
    private Integer runTimes;
    private Flag recovery;
    private Date processStartTime;

    @Override
    public ListenerEventType getEventType() {
        return ListenerEventType.PROCESS_START;
    }

    @Override
    public String getTitle() {
        return String.format("process start: %s", processName);
    }
}

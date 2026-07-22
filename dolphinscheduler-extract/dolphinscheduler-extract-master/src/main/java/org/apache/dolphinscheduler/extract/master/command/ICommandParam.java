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

package org.apache.dolphinscheduler.extract.master.command;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "commandType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScheduleWorkflowCommandParam.class, name = "SCHEDULER"),
        @JsonSubTypes.Type(value = RunWorkflowCommandParam.class, name = "START_PROCESS"),
        @JsonSubTypes.Type(value = BackfillWorkflowCommandParam.class, name = "COMPLEMENT_DATA"),
        @JsonSubTypes.Type(value = ReRunWorkflowCommandParam.class, name = "REPEAT_RUNNING"),
        @JsonSubTypes.Type(value = RecoverFailureTaskCommandParam.class, name = "START_FAILURE_TASK_PROCESS"),
        @JsonSubTypes.Type(value = WorkflowFailoverCommandParam.class, name = "RECOVER_TOLERANCE_FAULT"),
})
public interface ICommandParam {

    /**
     * The task which need to be as the beginning of the workflow.
     */
    List<Long> getStartNodes();

    /**
     * The command params.
     */
    List<Property> getCommandParams();

    /**
     * Get the time zone.
     * todo: we should remove this field.
     */
    String getTimeZone();

    /**
     * Whether the command is used to trigger a sub workflow instance.
     */
    boolean isSubWorkflowInstance();

    /**
     * Get the command type.
     */
    CommandType getCommandType();

}

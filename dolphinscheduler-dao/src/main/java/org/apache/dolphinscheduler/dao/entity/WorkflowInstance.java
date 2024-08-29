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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@TableName("t_ds_workflow_instance")
public class WorkflowInstance {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long workflowDefinitionCode;

    private int workflowDefinitionVersion;

    private Long projectCode;

    private WorkflowExecutionStatus state;

    private String stateHistory;

    @TableField(exist = false)
    private List<StateDesc> stateDescList;

    private Flag recovery;
    private Date startTime;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endTime;

    private int runTimes;

    private String name;

    private String host;

    @Deprecated
    @TableField(exist = false)
    private WorkflowDefinition workflowDefinition;

    private CommandType commandType;

    private String commandParam;

    private TaskDependType taskDependType;

    @Deprecated
    private int maxTryTimes;

    private FailureStrategy failureStrategy;

    private WarningType warningType;

    private Integer warningGroupId;

    private Date scheduleTime;

    private Date commandStartTime;

    /**
     * user define parameters string
     */
    private String globalParams;

    @TableField(exist = false)
    private DagData dagData;

    private int executorId;

    private String executorName;

    private String tenantCode;

    @TableField(exist = false)
    private String queue;

    private Flag isSubWorkflow;

    /**
     * task locations for web
     */
    @TableField(exist = false)
    private String locations;

    private String historyCmd;

    @TableField(exist = false)
    private String dependenceScheduleTimes;

    /**
     * workflow execution duration
     *
     * @return
     */
    @TableField(exist = false)
    private String duration;

    private Priority workflowInstancePriority;

    private String workerGroup;

    private Long environmentCode;

    private int timeout;

    private String varPool;

    @Deprecated
    private int nextWorkflowInstanceId;

    private int dryRun;

    private Date restartTime;

    private int testFlag;

    /**
     * set the process name with process define version and timestamp
     *
     * @param workflowDefinition processDefinition
     */
    public WorkflowInstance(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
        // todo: the name is not unique
        this.name = String.join("-",
                workflowDefinition.getName(),
                String.valueOf(workflowDefinition.getVersion()),
                DateUtils.getCurrentTimeStamp());
    }

    /**
     * add command to history
     *
     * @param cmd cmd
     */
    public void addHistoryCmd(CommandType cmd) {
        if (!Strings.isNullOrEmpty(this.historyCmd)) {
            this.historyCmd = String.format("%s,%s", this.historyCmd, cmd.toString());
        } else {
            this.historyCmd = cmd.toString();
        }
    }

    /**
     * check this process is start complement data
     *
     * @return whether complement data
     */
    public boolean isComplementData() {
        if (Strings.isNullOrEmpty(this.historyCmd)) {
            return false;
        }
        return historyCmd.startsWith(CommandType.COMPLEMENT_DATA.toString());
    }

    /**
     * get current command type,
     * if start with complement data,return complement
     *
     * @return CommandType
     */
    public CommandType getCmdTypeIfComplement() {
        if (isComplementData()) {
            return CommandType.COMPLEMENT_DATA;
        }
        return commandType;
    }

    /**
     * set state with desc
     * @param state
     * @param stateDesc
     */
    public void setStateWithDesc(WorkflowExecutionStatus state, String stateDesc) {
        this.setState(state);
        if (StringUtils.isEmpty(this.getStateHistory())) {
            stateDescList = new ArrayList<>();
        } else if (stateDescList == null) {
            stateDescList = JSONUtils.toList(this.getStateHistory(), StateDesc.class);
        }
        stateDescList.add(new StateDesc(new Date(), state, stateDesc));
        this.setStateHistory(JSONUtils.toJsonString(stateDescList));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateDesc {

        Date time;
        WorkflowExecutionStatus state;
        String desc;
    }
}

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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.dolphinscheduler.common.enums.*;

import java.util.Date;

/**
 * process instance
 */
@TableName("t_ds_process_instance")
public class ProcessInstance {

    /**
     * id
     */
    @TableId(value="id", type=IdType.AUTO)
    private int id;
    /**
     * process definition id
     */
    private int processDefinitionId;
    /**
     * process state
     */
    private ExecutionStatus state;
    /**
     * recovery flag for failover
     */
    private Flag recovery;
    /**
     * start time
     */
    private Date startTime;

    /**
     * end time
     */
    private Date endTime;

    /**
     * run time
     */
    private int runTimes;

    /**
     * name
     */
    private String name;

    /**
     * host
     */
    private String host;

    /**
     * process definition structure
     */
    @TableField(exist = false)
    private ProcessDefinition processDefinition;
    /**
     * process command type
     */
    private CommandType commandType;

    /**
     * command parameters
     */
    private String commandParam;

    /**
     * node depend type
     */
    private TaskDependType taskDependType;

    /**
     * task max try times
     */
    private int maxTryTimes;

    /**
     * failure strategy when task failed.
     */
    private FailureStrategy failureStrategy;

    /**
     *  warning type
     */
    private WarningType warningType;

    /**
     * warning group
     */
    private Integer warningGroupId;

    /**
     * schedule time
     */
    private Date scheduleTime;

    /**
     * command start time
     */
    private Date commandStartTime;

    /**
     * user define parameters string
     */
    private String globalParams;

    /**
     * process instance json
     */
    private String processInstanceJson;

    /**
     * executor id
     */
    private int executorId;

    /**
     * tenant code
     */
    @TableField(exist = false)
    private String tenantCode;

    /**
     * queue
     */
    @TableField(exist = false)
    private String queue;

    /**
     * process is sub process
     */
    private Flag isSubProcess;

    /**
     * task locations for web
     */
    private String locations;

    /**
     * task connects for web
     */
    private String connects;

    /**
     * history command
     */
    private String historyCmd;

    /**
     * depend processes schedule time
     */
    private String dependenceScheduleTimes;

    /**
     * process duration
     * @return
     */
    @TableField(exist = false)
    private Long duration;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     * worker group id
     */
    private int workerGroupId;

    /**
     * process timeout for warning
     */
    private int timeout;

    /**
     * tenant id
     */
    private int tenantId;

    /**
     * worker group name. for api.
     */
    @TableField(exist = false)
    private String workerGroupName;

    /**
     * receivers for api
     */
    @TableField(exist = false)
    private String receivers;

    /**
     * receivers cc for api
     */
    @TableField(exist = false)
    private String receiversCc;

    public ProcessInstance(){

    }

    /**
     * set the process name with process define version and timestamp
     * @param processDefinition processDefinition
     */
    public ProcessInstance(ProcessDefinition processDefinition){
        this.processDefinition = processDefinition;
        this.name = processDefinition.getName() + "-" +
                processDefinition.getVersion() + "-" +
                System.currentTimeMillis();
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(int processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public ExecutionStatus getState() {
        return state;
    }

    public void setState(ExecutionStatus state) {
        this.state = state;
    }

    public Flag getRecovery() {
        return recovery;
    }

    public void setRecovery(Flag recovery) {
        this.recovery = recovery;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(int runTimes) {
        this.runTimes = runTimes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public String getCommandParam() {
        return commandParam;
    }

    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }

    public TaskDependType getTaskDependType() {
        return taskDependType;
    }

    public void setTaskDependType(TaskDependType taskDependType) {
        this.taskDependType = taskDependType;
    }


    public int getMaxTryTimes() {
        return maxTryTimes;
    }

    public void setMaxTryTimes(int maxTryTimes) {
        this.maxTryTimes = maxTryTimes;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }


    public boolean IsProcessInstanceStop(){
        return this.state.typeIsFinished();
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public Integer getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(Integer warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getCommandStartTime() {
        return commandStartTime;
    }

    public void setCommandStartTime(Date commandStartTime) {
        this.commandStartTime = commandStartTime;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public String getProcessInstanceJson() {
        return processInstanceJson;
    }

    public void setProcessInstanceJson(String processInstanceJson) {
        this.processInstanceJson = processInstanceJson;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }


    public Flag getIsSubProcess() {
        return isSubProcess;
    }

    public void setIsSubProcess(Flag isSubProcess) {
        this.isSubProcess = isSubProcess;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }
    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getConnects() {
        return connects;
    }

    public void setConnects(String connects) {
        this.connects = connects;
    }

    public String getHistoryCmd() {
        return historyCmd;
    }

    public void setHistoryCmd(String historyCmd) {
        this.historyCmd = historyCmd;
    }

    /**
     * add command to history
     * @param cmd cmd
     */
    public void addHistoryCmd(CommandType cmd){
        if(StringUtils.isNotEmpty(this.historyCmd)){
            this.historyCmd = String.format("%s,%s", this.historyCmd, cmd.toString());
        }else{
            this.historyCmd = cmd.toString();
        }
    }

    /**
     * check this process is start complement data
     * @return whether complement data
     */
    public Boolean isComplementData(){
        if(!StringUtils.isNotEmpty(this.historyCmd)){
            return false;
        }
        return historyCmd.startsWith(CommandType.COMPLEMENT_DATA.toString());
    }

    /**
     * get current command type,
     * if start with complement data,return complement
     * @return CommandType
     */
    public CommandType getCmdTypeIfComplement(){
        if(isComplementData()){
            return CommandType.COMPLEMENT_DATA;
        }
        return commandType;
    }

    public String getDependenceScheduleTimes() {
        return dependenceScheduleTimes;
    }

    public void setDependenceScheduleTimes(String dependenceScheduleTimes) {
        this.dependenceScheduleTimes = dependenceScheduleTimes;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getWorkerGroupId() {
        return workerGroupId;
    }

    public void setWorkerGroupId(int workerGroupId) {
        this.workerGroupId = workerGroupId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return this.tenantId ;
    }

    public String getWorkerGroupName() {
        return workerGroupName;
    }

    public void setWorkerGroupName(String workerGroupName) {
        this.workerGroupName = workerGroupName;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id=" + id +
                ", processDefinitionId=" + processDefinitionId +
                ", state=" + state +
                ", recovery=" + recovery +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", runTimes=" + runTimes +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", processDefinition=" + processDefinition +
                ", commandType=" + commandType +
                ", commandParam='" + commandParam + '\'' +
                ", taskDependType=" + taskDependType +
                ", maxTryTimes=" + maxTryTimes +
                ", failureStrategy=" + failureStrategy +
                ", warningType=" + warningType +
                ", warningGroupId=" + warningGroupId +
                ", scheduleTime=" + scheduleTime +
                ", commandStartTime=" + commandStartTime +
                ", globalParams='" + globalParams + '\'' +
                ", processInstanceJson='" + processInstanceJson + '\'' +
                ", executorId=" + executorId +
                ", tenantCode='" + tenantCode + '\'' +
                ", queue='" + queue + '\'' +
                ", isSubProcess=" + isSubProcess +
                ", locations='" + locations + '\'' +
                ", connects='" + connects + '\'' +
                ", historyCmd='" + historyCmd + '\'' +
                ", dependenceScheduleTimes='" + dependenceScheduleTimes + '\'' +
                ", duration=" + duration +
                ", processInstancePriority=" + processInstancePriority +
                ", workerGroupId=" + workerGroupId +
                ", timeout=" + timeout +
                ", tenantId=" + tenantId +
                ", workerGroupName='" + workerGroupName + '\'' +
                ", receivers='" + receivers + '\'' +
                ", receiversCc='" + receiversCc + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInstance that = (ProcessInstance) o;

        if (id != that.id) return false;
        if (processDefinitionId != that.processDefinitionId) return false;
        if (runTimes != that.runTimes) return false;
        if (maxTryTimes != that.maxTryTimes) return false;
        if (executorId != that.executorId) return false;
        if (workerGroupId != that.workerGroupId) return false;
        if (timeout != that.timeout) return false;
        if (tenantId != that.tenantId) return false;
        if (state != that.state) return false;
        if (recovery != that.recovery) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (processDefinition != null ? !processDefinition.equals(that.processDefinition) : that.processDefinition != null)
            return false;
        if (commandType != that.commandType) return false;
        if (commandParam != null ? !commandParam.equals(that.commandParam) : that.commandParam != null) return false;
        if (taskDependType != that.taskDependType) return false;
        if (failureStrategy != that.failureStrategy) return false;
        if (warningType != that.warningType) return false;
        if (warningGroupId != null ? !warningGroupId.equals(that.warningGroupId) : that.warningGroupId != null)
            return false;
        if (scheduleTime != null ? !scheduleTime.equals(that.scheduleTime) : that.scheduleTime != null) return false;
        if (commandStartTime != null ? !commandStartTime.equals(that.commandStartTime) : that.commandStartTime != null)
            return false;
        if (globalParams != null ? !globalParams.equals(that.globalParams) : that.globalParams != null) return false;
        if (processInstanceJson != null ? !processInstanceJson.equals(that.processInstanceJson) : that.processInstanceJson != null)
            return false;
        if (tenantCode != null ? !tenantCode.equals(that.tenantCode) : that.tenantCode != null) return false;
        if (queue != null ? !queue.equals(that.queue) : that.queue != null) return false;
        if (isSubProcess != that.isSubProcess) return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null) return false;
        if (connects != null ? !connects.equals(that.connects) : that.connects != null) return false;
        if (historyCmd != null ? !historyCmd.equals(that.historyCmd) : that.historyCmd != null) return false;
        if (dependenceScheduleTimes != null ? !dependenceScheduleTimes.equals(that.dependenceScheduleTimes) : that.dependenceScheduleTimes != null)
            return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
        if (processInstancePriority != that.processInstancePriority) return false;
        if (workerGroupName != null ? !workerGroupName.equals(that.workerGroupName) : that.workerGroupName != null)
            return false;
        if (receivers != null ? !receivers.equals(that.receivers) : that.receivers != null) return false;
        return receiversCc != null ? receiversCc.equals(that.receiversCc) : that.receiversCc == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + processDefinitionId;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (recovery != null ? recovery.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + runTimes;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (processDefinition != null ? processDefinition.hashCode() : 0);
        result = 31 * result + (commandType != null ? commandType.hashCode() : 0);
        result = 31 * result + (commandParam != null ? commandParam.hashCode() : 0);
        result = 31 * result + (taskDependType != null ? taskDependType.hashCode() : 0);
        result = 31 * result + maxTryTimes;
        result = 31 * result + (failureStrategy != null ? failureStrategy.hashCode() : 0);
        result = 31 * result + (warningType != null ? warningType.hashCode() : 0);
        result = 31 * result + (warningGroupId != null ? warningGroupId.hashCode() : 0);
        result = 31 * result + (scheduleTime != null ? scheduleTime.hashCode() : 0);
        result = 31 * result + (commandStartTime != null ? commandStartTime.hashCode() : 0);
        result = 31 * result + (globalParams != null ? globalParams.hashCode() : 0);
        result = 31 * result + (processInstanceJson != null ? processInstanceJson.hashCode() : 0);
        result = 31 * result + executorId;
        result = 31 * result + (tenantCode != null ? tenantCode.hashCode() : 0);
        result = 31 * result + (queue != null ? queue.hashCode() : 0);
        result = 31 * result + (isSubProcess != null ? isSubProcess.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (connects != null ? connects.hashCode() : 0);
        result = 31 * result + (historyCmd != null ? historyCmd.hashCode() : 0);
        result = 31 * result + (dependenceScheduleTimes != null ? dependenceScheduleTimes.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (processInstancePriority != null ? processInstancePriority.hashCode() : 0);
        result = 31 * result + workerGroupId;
        result = 31 * result + timeout;
        result = 31 * result + tenantId;
        result = 31 * result + (workerGroupName != null ? workerGroupName.hashCode() : 0);
        result = 31 * result + (receivers != null ? receivers.hashCode() : 0);
        result = 31 * result + (receiversCc != null ? receiversCc.hashCode() : 0);
        return result;
    }
}

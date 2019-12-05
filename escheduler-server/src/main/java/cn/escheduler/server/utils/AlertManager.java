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
package cn.escheduler.server.utils;


import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.enums.CommandType;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.model.Alert;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * alert manager
 */
public class AlertManager {

    private static final Logger logger = LoggerFactory.getLogger(AlertManager.class);

    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);


    /**
     *  command type convert chinese
     * @param commandType
     * @return
     */
    private String getCommandCnName(CommandType commandType) {
        switch (commandType) {
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return "recover tolerance fault process";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended process";
            case START_CURRENT_TASK_PROCESS:
                return "start current task process";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task process";
            case START_PROCESS:
                return "start process";
            case REPEAT_RUNNING:
                return "repeat running";
            case SCHEDULER:
                return "scheduler";
            case COMPLEMENT_DATA:
                return "complement data";
            case PAUSE:
                return "pause";
            case STOP:
                return "stop";
            default:
                return "unknown type";
        }
    }

    /**
     *  process instance format
     */
    private static final String PROCESS_INSTANCE_FORMAT =
            "\"Id:%d\"," +
            "\"Name:%s\"," +
            "\"Job type: %s\"," +
            "\"State: %s\"," +
            "\"Recovery:%s\"," +
            "\"Run time: %d\"," +
            "\"Start time: %s\"," +
            "\"End time: %s\"," +
            "\"Host: %s\"" ;

    /**
     *  get process instance content
     * @param processInstance
     * @return
     */
    public String getContentProcessInstance(ProcessInstance processInstance,
                                            List<TaskInstance> taskInstances){

        String res = "";
        if(processInstance.getState().typeIsSuccess()){
            res = String.format(PROCESS_INSTANCE_FORMAT,
                    processInstance.getId(),
                    processInstance.getName(),
                    getCommandCnName(processInstance.getCommandType()),
                    processInstance.getState().toString(),
                    processInstance.getRecovery().toString(),
                    processInstance.getRunTimes(),
                    DateUtils.dateToString(processInstance.getStartTime()),
                    DateUtils.dateToString(processInstance.getEndTime()),
                    processInstance.getHost()

            );
            res = "[" + res + "]";
        }else if(processInstance.getState().typeIsFailure()){

            List<LinkedHashMap> failedTaskList = new ArrayList<>();

            for(TaskInstance task : taskInstances){
                if(task.getState().typeIsSuccess()){
                    continue;
                }
                LinkedHashMap<String, String> failedTaskMap = new LinkedHashMap();
                failedTaskMap.put("task id", String.valueOf(task.getId()));
                failedTaskMap.put("task name", task.getName());
                failedTaskMap.put("task type", task.getTaskType());
                failedTaskMap.put("task state", task.getState().toString());
                failedTaskMap.put("task start time", DateUtils.dateToString(task.getStartTime()));
                failedTaskMap.put("task end time", DateUtils.dateToString(task.getEndTime()));
                failedTaskMap.put("host", task.getHost());
                failedTaskMap.put("log path", task.getLogPath());
                failedTaskList.add(failedTaskMap);
            }
            res = JSONUtils.toJson(failedTaskList);
        }

        return res;
    }

    /**
     *  getting worker fault tolerant content
     * @param processInstance
     * @param toleranceTaskList
     * @return
     */
    private String getWorkerToleranceContent(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList){

        List<LinkedHashMap<String, String>> toleranceTaskInstanceList =  new ArrayList<>();

        for(TaskInstance taskInstance: toleranceTaskList){
            LinkedHashMap<String, String> toleranceWorkerContentMap = new LinkedHashMap();
            toleranceWorkerContentMap.put("process name", processInstance.getName());
            toleranceWorkerContentMap.put("task name", taskInstance.getName());
            toleranceWorkerContentMap.put("host", taskInstance.getHost());
            toleranceWorkerContentMap.put("task retry times", String.valueOf(taskInstance.getRetryTimes()));
            toleranceTaskInstanceList.add(toleranceWorkerContentMap);
        }
        return JSONUtils.toJson(toleranceTaskInstanceList);
    }

    /**
     * send worker alert fault tolerance
     * @param processInstance
     * @param toleranceTaskList
     */
    public void sendAlertWorkerToleranceFault(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList){
        Alert alert = new Alert();
        alert.setTitle("worker fault tolerance");
        alert.setShowType(ShowType.TABLE);
        String content = getWorkerToleranceContent(processInstance, toleranceTaskList);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setCreateTime(new Date());
        alert.setAlertGroupId(processInstance.getWarningGroupId() == null ? 1:processInstance.getWarningGroupId());
        alert.setReceivers(processInstance.getProcessDefinition().getReceivers());
        alert.setReceiversCc(processInstance.getProcessDefinition().getReceiversCc());

        alertDao.addAlert(alert);
        logger.info("add alert to db , alert : {}", alert.toString());

    }

    /**
     * send process instance alert
     * @param processInstance
     */
    public void sendAlertProcessInstance(ProcessInstance processInstance,
                                         List<TaskInstance> taskInstances){

        boolean sendWarnning = false;
        WarningType warningType = processInstance.getWarningType();
        switch (warningType){
            case ALL:
                if(processInstance.getState().typeIsFinished()){
                    sendWarnning = true;
                }
                break;
            case SUCCESS:
                if(processInstance.getState().typeIsSuccess()){
                    sendWarnning = true;
                }
                break;
            case FAILURE:
                if(processInstance.getState().typeIsFailure()){
                    sendWarnning = true;
                }
                break;
                default:
        }
        if(!sendWarnning){
            return;
        }
        Alert alert = new Alert();


        String cmdName = getCommandCnName(processInstance.getCommandType());
        String success = processInstance.getState().typeIsSuccess() ? "success" :"failed";
        alert.setTitle(cmdName + success);
        ShowType showType = processInstance.getState().typeIsSuccess() ? ShowType.TEXT : ShowType.TABLE;
        alert.setShowType(showType);
        String content = getContentProcessInstance(processInstance, taskInstances);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setReceivers(processInstance.getProcessDefinition().getReceivers());
        alert.setReceiversCc(processInstance.getProcessDefinition().getReceiversCc());

        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert.toString());
    }

    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        alertDao.sendProcessTimeoutAlert(processInstance, processDefinition);
    }
}

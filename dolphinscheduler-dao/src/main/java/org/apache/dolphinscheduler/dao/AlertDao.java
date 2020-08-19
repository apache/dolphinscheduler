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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    @Override
    protected void init() {
        alertMapper = ConnectionFactory.getInstance().getMapper(AlertMapper.class);
        userAlertGroupMapper = ConnectionFactory.getInstance().getMapper(UserAlertGroupMapper.class);
    }

    /**
     * insert alert
     *
     * @param alert alert
     * @return add alert result
     */
    public int addAlert(Alert alert) {
        return alertMapper.insert(alert);
    }

    /**
     * update alert
     *
     * @param alertStatus alertStatus
     * @param log log
     * @param id id
     * @return update alert result
     */
    public int updateAlert(AlertStatus alertStatus, String log, int id) {
        Alert alert = alertMapper.selectById(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * query user list by alert group id
     *
     * @param alerGroupId alerGroupId
     * @return user list
     */
    public List<User> queryUserByAlertGroupId(int alerGroupId) {

        return userAlertGroupMapper.listUserByAlertgroupId(alerGroupId);
    }

    /**
     * MasterServer or WorkerServer stoped
     *
     * @param alertgroupId alertgroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendServerStopedAlert(int alertgroupId, String host, String serverType) {
        Alert alert = new Alert();
        List<LinkedHashMap> serverStopList = new ArrayList<>(1);
        LinkedHashMap<String, String> serverStopedMap = new LinkedHashMap();
        serverStopedMap.put("type", serverType);
        serverStopedMap.put("host", host);
        serverStopedMap.put("event", "server down");
        serverStopedMap.put("warning level", "serious");
        serverStopList.add(serverStopedMap);
        String content = JSONUtils.toJsonString(serverStopList);
        alert.setTitle("Fault tolerance warning");
        saveTaskTimeoutAlert(alert, content, alertgroupId, null, null);
    }

    /**
     * process time out alert
     *
     * @param processInstance processInstance
     * @param processDefinition processDefinition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        int alertgroupId = processInstance.getWarningGroupId();
        String receivers = processDefinition.getReceivers();
        String receiversCc = processDefinition.getReceiversCc();
        Alert alert = new Alert();
        List<LinkedHashMap> processTimeoutList = new ArrayList<>(1);
        LinkedHashMap<String, String> processTimeoutMap = new LinkedHashMap();
        processTimeoutMap.put("id", String.valueOf(processInstance.getId()));
        processTimeoutMap.put("name", processInstance.getName());
        processTimeoutMap.put("event", "timeout");
        processTimeoutMap.put("warnLevel", "middle");
        processTimeoutList.add(processTimeoutMap);
        String content = JSONUtils.toJsonString(processTimeoutList);
        alert.setTitle("Process Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertgroupId, receivers, receiversCc);
    }

    private void saveTaskTimeoutAlert(Alert alert, String content, int alertgroupId,
                                      String receivers, String receiversCc) {
        alert.setShowType(ShowType.TABLE);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(alertgroupId);
        if (StringUtils.isNotEmpty(receivers)) {
            alert.setReceivers(receivers);
        }
        if (StringUtils.isNotEmpty(receiversCc)) {
            alert.setReceiversCc(receiversCc);
        }
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alertMapper.insert(alert);
    }

    /**
     * task timeout warn
     *
     * @param alertgroupId alertgroupId
     * @param receivers receivers
     * @param receiversCc receiversCc
     * @param processInstanceId processInstanceId
     * @param processInstanceName processInstanceName
     * @param taskId taskId
     * @param taskName taskName
     */
    public void sendTaskTimeoutAlert(int alertgroupId, String receivers, String receiversCc, int processInstanceId,
                                     String processInstanceName, int taskId, String taskName) {
        Alert alert = new Alert();
        List<LinkedHashMap> taskTimeoutList = new ArrayList<>(1);
        LinkedHashMap<String, String> taskTimeoutMap = new LinkedHashMap();
        taskTimeoutMap.put("process instance id", String.valueOf(processInstanceId));
        taskTimeoutMap.put("process name", processInstanceName);
        taskTimeoutMap.put("task id", String.valueOf(taskId));
        taskTimeoutMap.put("task name", taskName);
        taskTimeoutMap.put("event", "timeout");
        taskTimeoutMap.put("warnLevel", "middle");
        taskTimeoutList.add(taskTimeoutMap);
        String content = JSONUtils.toJsonString(taskTimeoutList);
        alert.setTitle("Task Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertgroupId, receivers, receiversCc);
    }

    /**
     * list the alert information of waiting to be executed
     *
     * @return alert list
     */
    public List<Alert> listWaitExecutionAlert() {
        return alertMapper.listAlertByStatus(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * list user information by alert group id
     *
     * @param alertgroupId alertgroupId
     * @return user list
     */
    public List<User> listUserByAlertgroupId(int alertgroupId) {
        return userAlertGroupMapper.listUserByAlertgroupId(alertgroupId);
    }

    /**
     * for test
     *
     * @return AlertMapper
     */
    public AlertMapper getAlertMapper() {
        return alertMapper;
    }
}

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
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AlertDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    @Override
    protected void init() {
//        alertMapper = ConnectionFactory.getSqlSession().getMapper(AlertMapper.class);
//        userAlertGroupMapper = ConnectionFactory.getSqlSession().getMapper(UserAlertGroupMapper.class);
    }

    /**
     * insert alert
     * @param alert
     * @return
     */
    public int addAlert(Alert alert){
        return alertMapper.insert(alert);
    }

    /**
     * update alert
     * @param alertStatus
     * @param log
     * @param id
     * @return
     */
    public int updateAlert(AlertStatus alertStatus,String log,int id){
        Alert alert = alertMapper.selectById(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * query user list by alert group id
     * @param alerGroupId
     * @return
     */
    public List<User> queryUserByAlertGroupId(int alerGroupId){

        return userAlertGroupMapper.listUserByAlertgroupId(alerGroupId);
    }
    /**
     * MasterServer or WorkerServer stoped
     */
    public void sendServerStopedAlert(int alertgroupId,String host,String serverType){
        Alert alert = new Alert();
        String content = String.format("[{'type':'%s','host':'%s','event':'server down','warning level':'serious'}]",
                serverType, host);
        alert.setTitle("Fault tolerance warning");
        alert.setShowType(ShowType.TABLE);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(alertgroupId);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alertMapper.insert(alert);
    }

    /**
     * process time out alert
     * @param processInstance
     * @param processDefinition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition){
        int alertgroupId = processInstance.getWarningGroupId();
        String receivers = processDefinition.getReceivers();
        String receiversCc = processDefinition.getReceiversCc();
        Alert alert = new Alert();
        String content = String.format("[{'id':'%d','name':'%s','event':'timeout','warnLevel':'middle'}]",
                processInstance.getId(), processInstance.getName());
        alert.setTitle("Process Timeout Warn");
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
     */
    public void sendTaskTimeoutAlert(int alertgroupId,String receivers,String receiversCc,int taskId,String taskName){
        Alert alert = new Alert();
        String content = String.format("[{'id':'%d','name':'%s','event':'timeout','warnLevel':'middle'}]",taskId,taskName);
        alert.setTitle("Task Timeout Warn");
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
     * list the alert information of waiting to be executed
     * @return
     */
    public List<Alert> listWaitExecutionAlert(){
        return alertMapper.listAlertByStatus(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * list user information by alert group id
     * @param alergroupId
     * @return
     */
    public List<User> listUserByAlertgroupId(int alergroupId){
        return userAlertGroupMapper.listUserByAlertgroupId(alergroupId);
    }


}

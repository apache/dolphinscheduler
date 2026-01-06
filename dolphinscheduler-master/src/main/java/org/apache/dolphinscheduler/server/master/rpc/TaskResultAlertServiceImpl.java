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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.extract.master.ITaskResultAlertService;
import org.apache.dolphinscheduler.extract.master.transportor.TaskResultAlertRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskResultAlertResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskResultAlertServiceImpl implements ITaskResultAlertService {

    @Autowired
    private AlertDao alertDao;

    @Override
    public TaskResultAlertResponse reportTaskResultAlertToMaster(TaskResultAlertRequest taskResultAlertRequest) {
        log.info("Received TaskResultAlertRequest request{}", taskResultAlertRequest);

        try {
            Alert alert = new Alert();
            alert.setTitle(taskResultAlertRequest.getTitle());
            alert.setContent(taskResultAlertRequest.getContent());
            alert.setWarningType(WarningType.NONE);
            alert.setCreateTime(new Date());
            alert.setUpdateTime(new Date());
            alert.setAlertGroupId(taskResultAlertRequest.getAlertGroupId());
            alert.setWorkflowDefinitionCode(taskResultAlertRequest.getWorkflowDefinitionCode());
            alert.setWorkflowInstanceId(taskResultAlertRequest.getWorkflowInstanceId());
            Map<String, Object> info = new HashMap<>();
            info.put("taskInstanceId", taskResultAlertRequest.getTaskInstanceId());
            alert.setInfo(info);

            alert.setAlertType(AlertType.TASK_RESULT);
            alertDao.addAlert(alert);

            log.info("add alert success");
            return TaskResultAlertResponse.success();
        } catch (Exception ex) {
            log.error("add alert failed:{} ", ex.getMessage());
            return TaskResultAlertResponse.failed(ex.getMessage());
        }
    }
}

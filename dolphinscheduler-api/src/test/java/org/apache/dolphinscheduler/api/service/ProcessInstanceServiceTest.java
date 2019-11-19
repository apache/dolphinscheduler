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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProcessInstanceServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceServiceTest.class);

    @Autowired
    ProcessInstanceService processInstanceService;

    @Test
    public void viewVariables() {
        try {
            Map<String, Object> map = processInstanceService.viewVariables(-1);
            Assert.assertEquals(Status.SUCCESS, map.get(Constants.STATUS));
            logger.info(JSON.toJSONString(map));
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testDependResult(){
        String logString = "[INFO] 2019-03-19 17:11:08.475 org.apache.dolphinscheduler.server.worker.log.TaskLogger:[172] - [taskAppId=TASK_223_10739_452334] dependent item complete :|| 223-ALL-day-last1Day,SUCCESS\n" +
                "[INFO] 2019-03-19 17:11:08.476 org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread:[172] - task : 223_10739_452334 exit status code : 0\n" +
                "[root@node2 current]# ";
        try {
            Map<String, DependResult> resultMap =
                    processInstanceService.parseLogForDependentResult(logString);
            Assert.assertEquals(resultMap.size() , 1);
        } catch (IOException e) {

        }
    }

    @Test
    public void queryProcessInstanceList() throws Exception {

        User loginUser = new User();
        loginUser.setId(27);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> map = processInstanceService.queryProcessInstanceList(loginUser, "project_test1", 0, "", "", "", ExecutionStatus.FAILURE, "", 1, 5);

        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }
}
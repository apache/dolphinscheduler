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
package org.apache.dolphinscheduler.server.master.processor.queue;


import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringZKServer.class, TaskResponseService.class, ZookeeperRegistryCenter.class,
        ZookeeperCachedOperator.class, ZookeeperConfig.class, ZookeeperNodeManager.class, TaskResponseService.class})
public class TaskResponseServiceTest {

    @Autowired
    private TaskResponseService taskResponseService;

    @Test
    public void testAdd(){
        TaskResponseEvent taskResponseEvent = TaskResponseEvent.newAck(ExecutionStatus.RUNNING_EXEUTION, new Date(),
                "", "", "", 1,null);
        taskResponseService.addResponse(taskResponseEvent);
        Assert.assertTrue(taskResponseService.getEventQueue().size() == 1);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }
        //after sleep, inner worker will take the event
        Assert.assertTrue(taskResponseService.getEventQueue().size() == 0);
    }

    @Test
    public void testStop(){
        TaskResponseEvent taskResponseEvent = TaskResponseEvent.newAck(ExecutionStatus.RUNNING_EXEUTION, new Date(),
                "", "", "", 1,null);
        taskResponseService.addResponse(taskResponseEvent);
        taskResponseService.stop();
        Assert.assertTrue(taskResponseService.getEventQueue().size() == 0);
    }
}

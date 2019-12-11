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
package org.apache.dolphinscheduler.common.queue;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.IpUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.zk.StandaloneZKServerForTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * task queue test
 */
public class TaskQueueImplTest extends StandaloneZKServerForTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueImplTest.class);

    ITaskQueue tasksQueue = null;

    @Before
    public void before(){
        super.before();

        tasksQueue = TaskQueueFactory.getTaskQueueInstance();

        //clear all data
        tasksQueue.delete();

    }


    @After
    public void after(){
        //clear all data
        tasksQueue.delete();
    }


    @Test
    public void testAdd(){


        //add
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"1_0_1_1_-1");
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"0_1_1_1_-1");
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"0_0_0_1_" + IpUtils.ipToLong(OSUtils.getHost()));
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"1_2_1_1_" + IpUtils.ipToLong(OSUtils.getHost()) + 10);

        List<String> tasks = tasksQueue.poll(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, 1);

        if(tasks.size() <= 0){
            return;
        }

        //pop
        String node1 = tasks.get(0);

        assertEquals(node1,"0_0_0_1_" + IpUtils.ipToLong(OSUtils.getHost()));


    }



    /**
     * test one million data from zookeeper queue
     */
    @Ignore
    @Test
    public void extremeTest(){
        int total = 30 * 10000;

        for(int i = 0; i < total; i++) {
            for(int j = 0; j < total; j++) {
                //${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                //format ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                String formatTask = String.format("%s_%d_%s_%d", i, i + 1, j, j == 0 ?  0 : j + new Random().nextInt(100));
                tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, formatTask);
            }
        }

        String node1 = tasksQueue.poll(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, 1).get(0);
        assertEquals(node1,"0");

    }

}

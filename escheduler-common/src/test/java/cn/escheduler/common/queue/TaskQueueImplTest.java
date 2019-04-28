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
package cn.escheduler.common.queue;

import cn.escheduler.common.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * task queue test
 */
public class TaskQueueImplTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueImplTest.class);


    @Test
    public void testTaskQueue(){

        ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();
        //clear all data
        tasksQueue.delete();

        //add
        tasksQueue.add(Constants.SCHEDULER_TASKS_QUEUE,"1");
        tasksQueue.add(Constants.SCHEDULER_TASKS_QUEUE,"2");
        tasksQueue.add(Constants.SCHEDULER_TASKS_QUEUE,"3");
        tasksQueue.add(Constants.SCHEDULER_TASKS_QUEUE,"4");

        //pop
        String node1 = tasksQueue.poll(Constants.SCHEDULER_TASKS_QUEUE, false);
        assertEquals(node1,"1");
        String node2 = tasksQueue.poll(Constants.SCHEDULER_TASKS_QUEUE, false);
        assertEquals(node2,"2");

        //sadd
        String task1 = "1.1.1.1-1-mr";
        String task2 = "1.1.1.2-2-mr";
        String task3 = "1.1.1.3-3-mr";
        String task4 = "1.1.1.4-4-mr";
        String task5 = "1.1.1.5-5-mr";

        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task1);
        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task2);
        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task3);
        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task4);
        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task5);
        tasksQueue.sadd(Constants.SCHEDULER_TASKS_KILL,task5); //repeat task

        Assert.assertEquals(tasksQueue.smembers(Constants.SCHEDULER_TASKS_KILL).size(),5);
        logger.info(Arrays.toString(tasksQueue.smembers(Constants.SCHEDULER_TASKS_KILL).toArray()));
        //srem
        tasksQueue.srem(Constants.SCHEDULER_TASKS_KILL,task5);
        //smembers
        Assert.assertEquals(tasksQueue.smembers(Constants.SCHEDULER_TASKS_KILL).size(),4);
        logger.info(Arrays.toString(tasksQueue.smembers(Constants.SCHEDULER_TASKS_KILL).toArray()));


    }

    /**
     * test one million data from zookeeper queue
     */
    @Test
    public void extremeTest(){
        ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();
        //clear all data
        tasksQueue.delete();
        int total = 30 * 10000;

        for(int i = 0; i < total; i++)
        {
            for(int j = 0; j < total; j++) {
                //${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                //format ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                String formatTask = String.format("%s_%d_%s_%d", i, i + 1, j, j == 0 ?  0 : j + new Random().nextInt(100));
                tasksQueue.add(Constants.SCHEDULER_TASKS_QUEUE, formatTask);
            }
        }

        String node1 = tasksQueue.poll(Constants.SCHEDULER_TASKS_QUEUE, false);
        assertEquals(node1,"0");

        //clear all data
        tasksQueue.delete();



    }

}

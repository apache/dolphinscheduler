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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * task queue test
 */
@Ignore
public class TaskQueueZKImplTest extends BaseTaskQueueTest  {

    @Before
    public void before(){

        //clear all data
        tasksQueue.delete();
    }

    @After
    public void after(){
        //clear all data
        tasksQueue.delete();
    }

    /**
     * test take out all the elements
     */
    @Test
    public  void getAllTasks(){

        //add
        init();
        // get all
        List<String> allTasks = tasksQueue.getAllTasks(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
        assertEquals(allTasks.size(),2);
        //delete all
        tasksQueue.delete();
        allTasks = tasksQueue.getAllTasks(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
        assertEquals(allTasks.size(),0);
    }

    /**
     * test check task exists in the task queue or not
     */
    @Test
    public  void checkTaskExists(){

        String task= "1_0_1_1_-1";
        //add
        init();
        // check Exist true
        boolean taskExists = tasksQueue.checkTaskExists(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, task);
        assertTrue(taskExists);

        //remove task
        tasksQueue.removeNode(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        // check Exist false
        taskExists = tasksQueue.checkTaskExists(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, task);
        assertFalse(taskExists);
    }

    /**
     * test add  element to the queue
     */
    @Test
    public void add(){

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
     *  test  element pops out of the queue
     */
    @Test
    public  void poll(){
        
        //add
        init();
        List<String> taskList = tasksQueue.poll(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, 2);
        assertEquals(taskList.size(),2);

        assertEquals(taskList.get(0),"0_1_1_1_-1");
        assertEquals(taskList.get(1),"1_0_1_1_-1");
    }

    /**
     * test remove  element from queue
     */
    @Test
    public void removeNode(){
        String task = "1_0_1_1_-1";
        //add
        init();
        tasksQueue.removeNode(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        assertFalse(tasksQueue.checkTaskExists(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task));
    }

    /**
     * test add an element to the set
     */
    @Test
    public void sadd(){

        String task = "1_0_1_1_-1";
        tasksQueue.sadd(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        //check size
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),1);
    }


    /**
     * test delete the value corresponding to the key in the set
     */
    @Test
    public void srem(){

        String task = "1_0_1_1_-1";
        tasksQueue.sadd(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        //check size
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),1);
        //remove and get size
        tasksQueue.srem(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),0);
    }

    /**
     * test gets all the elements of the set based on the key
     */
    @Test
    public void smembers(){

        //first init
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),0);
        //add
        String task = "1_0_1_1_-1";
        tasksQueue.sadd(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        //check size
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),1);
        //add
        task = "0_1_1_1_";
        tasksQueue.sadd(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,task);
        //check size
        assertEquals(tasksQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_QUEUE).size(),2);
    }


    /**
     * init data
     */
    private void init(){
        //add
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"1_0_1_1_-1");
        tasksQueue.add(Constants.DOLPHINSCHEDULER_TASKS_QUEUE,"0_1_1_1_-1");
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

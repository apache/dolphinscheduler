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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.Bytes;
import org.apache.dolphinscheduler.common.utils.IpUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.zk.AbstractZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton of a task queue implemented with zookeeper
 * tasks queue implemention
 */
public class TaskQueueZkImpl extends AbstractZKClient implements ITaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueZkImpl.class);

    private static volatile TaskQueueZkImpl instance;

    private TaskQueueZkImpl(){
        init();
    }

    public static TaskQueueZkImpl getInstance(){
        if (null == instance) {
            synchronized (TaskQueueZkImpl.class) {
                if(null == instance) {
                    instance = new TaskQueueZkImpl();
                }
            }
        }
        return instance;
    }


    /**
     * get all tasks from tasks queue
     * @param key   task queue name
     * @return
     */
    @Override
    public List<String> getAllTasks(String key) {
        try {
            List<String> list = getZkClient().getChildren().forPath(getTasksPath(key));

            return list;
        } catch (Exception e) {
            logger.error("get all tasks from tasks queue exception",e);
        }

        return new ArrayList<String>();
    }

    /**
     * check task exists in the task queue or not
     *
     * @param key       queue name
     * @param task      ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
     * @return true if exists in the queue
     */
    @Override
    public boolean checkTaskExists(String key, String task) {
        String taskPath = getTasksPath(key) + Constants.SINGLE_SLASH + task;

        try {
            Stat stat = zkClient.checkExists().forPath(taskPath);

            if(null == stat){
                logger.info("check task:{} not exist in task queue",task);
                return false;
            }else{
                logger.info("check task {} exists in task queue ",task);
                return true;
            }

        } catch (Exception e) {
            logger.info(String.format("task {} check exists in task queue exception ", task), e);
        }

        return false;
    }


    /**
     * add task to tasks queue
     *
     * @param key      task queue name
     * @param value    ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}_host1,host2,...
     */
    @Override
    public boolean add(String key, String value){
        try {
            String taskIdPath = getTasksPath(key) + Constants.SINGLE_SLASH + value;
            String result = getZkClient().create().withMode(CreateMode.PERSISTENT).forPath(taskIdPath, Bytes.toBytes(value));

            logger.info("add task : {} to tasks queue , result success",result);
            return true;
        } catch (Exception e) {
            logger.error("add task to tasks queue exception",e);
            return false;
        }

    }


    /**
     * An element pops out of the queue <p>
     * note:
     *   ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}_host1,host2,...
     *   The tasks with the highest priority are selected by comparing the priorities of the above four levels from high to low.
     *
     * @param  key  task queue name
     * @param  tasksNum    how many elements to poll
     * @return the task ids  to be executed
     */
    @Override
    public List<String> poll(String key, int tasksNum) {
        try{
            CuratorFramework zk = getZkClient();
            String tasksQueuePath = getTasksPath(key) + Constants.SINGLE_SLASH;
            List<String> list = zk.getChildren().forPath(getTasksPath(key));

            if(list != null && list.size() > 0){

                String workerIp = OSUtils.getHost();
                String workerIpLongStr = String.valueOf(IpUtils.ipToLong(workerIp));

                int size = list.size();


                Set<String> taskTreeSet = new TreeSet<>(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {

                        String s1 = o1;
                        String s2 = o2;
                        String[] s1Array = s1.split(Constants.UNDERLINE);
                        if(s1Array.length>4){
                            // warning: if this length > 5, need to be changed
                            s1 = s1.substring(0, s1.lastIndexOf(Constants.UNDERLINE) );
                        }

                        String[] s2Array = s2.split(Constants.UNDERLINE);
                        if(s2Array.length>4){
                            // warning: if this length > 5, need to be changed
                            s2 = s2.substring(0, s2.lastIndexOf(Constants.UNDERLINE) );
                        }

                        return s1.compareTo(s2);
                    }
                });

                for (int i = 0; i < size; i++) {

                    String taskDetail = list.get(i);
                    String[] taskDetailArrs = taskDetail.split(Constants.UNDERLINE);

                    //forward compatibility 向前版本兼容
                    if(taskDetailArrs.length >= 4){

                        //format ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                        String formatTask = String.format("%s_%010d_%s_%010d", taskDetailArrs[0], Long.parseLong(taskDetailArrs[1]), taskDetailArrs[2], Long.parseLong(taskDetailArrs[3]));
                        if(taskDetailArrs.length > 4){
                            String taskHosts = taskDetailArrs[4];

                            //task can assign to any worker host if equals default ip value of worker server
                            if(!taskHosts.equals(String.valueOf(Constants.DEFAULT_WORKER_ID))){
                                String[] taskHostsArr = taskHosts.split(Constants.COMMA);
                                if(!Arrays.asList(taskHostsArr).contains(workerIpLongStr)){
                                    continue;
                                }
                            }
                            formatTask += Constants.UNDERLINE + taskDetailArrs[4];
                        }
                        taskTreeSet.add(formatTask);

                    }

                }

                List<String> taskslist = getTasksListFromTreeSet(tasksNum, taskTreeSet);

                logger.info("consume tasks: {},there still have {} tasks need to be executed", Arrays.toString(taskslist.toArray()), size - taskslist.size());

                return taskslist;
            }else{
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            }

        } catch (Exception e) {
            logger.error("add task to tasks queue exception",e);
        }
        return new ArrayList<String>();
    }


    /**
     * get task list from tree set
     *
     * @param tasksNum
     * @param taskTreeSet
     */
    public List<String> getTasksListFromTreeSet(int tasksNum, Set<String> taskTreeSet) {
        Iterator<String> iterator = taskTreeSet.iterator();
        int j = 0;
        List<String> taskslist = new ArrayList<>(tasksNum);
        while(iterator.hasNext()){
            if(j++ >= tasksNum){
                break;
            }
            String task = iterator.next();
            taskslist.add(getOriginTaskFormat(task));
        }
        return taskslist;
    }

    /**
     * format ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
     * processInstanceId and task id need to be convert to int.
     * @param formatTask
     * @return
     */
    private String getOriginTaskFormat(String formatTask){
        String[] taskArray = formatTask.split(Constants.UNDERLINE);
        if(taskArray.length< 4){
            return formatTask;
        }
        int processInstanceId = Integer.parseInt(taskArray[1]);
        int taskId = Integer.parseInt(taskArray[3]);

        StringBuilder sb = new StringBuilder(50);
        String destTask = String.format("%s_%s_%s_%s", taskArray[0], processInstanceId, taskArray[2], taskId);

        sb.append(destTask);

        if(taskArray.length > 4){
            for(int index = 4; index < taskArray.length; index++){
                sb.append(Constants.UNDERLINE).append(taskArray[index]);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean removeNode(String key, String nodeValue){

        CuratorFramework zk = getZkClient();
        String tasksQueuePath = getTasksPath(key) + Constants.SINGLE_SLASH;
        String taskIdPath = tasksQueuePath + nodeValue;
        logger.info("consume task {}", taskIdPath);
        try{
            Stat stat = zk.checkExists().forPath(taskIdPath);
            if(stat != null){
                zk.delete().forPath(taskIdPath);
            }
            return true;
        }catch(Exception e){
            logger.error(String.format("delete task:%s from zookeeper fail, exception:" ,nodeValue) ,e);
            return false;
        }

    }



    /**
     * In order to be compatible with redis implementation
     *
     * To be compatible with the redis implementation, add an element to the set
     * @param key   The key is the kill/cancel queue path name
     * @param value host-taskId  The name of the zookeeper node
     * @return
     */
    @Override
    public boolean sadd(String key, String value) {
        try {

            if(value != null && value.trim().length() > 0){
                String path = getTasksPath(key) + Constants.SINGLE_SLASH;
                CuratorFramework zk = getZkClient();
                Stat stat = zk.checkExists().forPath(path + value);

                if(null == stat){
                    String result = zk.create().withMode(CreateMode.PERSISTENT).forPath(path + value,Bytes.toBytes(value));
                    logger.info("add task:{} to tasks set result:{} ",value,result);
                }else{
                    logger.info("task {} exists in tasks set ",value);
                }

            }else{
                logger.warn("add host-taskId:{} to tasks set is empty ",value);
            }
            return true;

        } catch (Exception e) {
            logger.error("add task to tasks set exception",e);
            return false;
        }
    }


    /**
     * delete the value corresponding to the key in the set
     * @param key   The key is the kill/cancel queue path name
     * @param value host-taskId-taskType The name of the zookeeper node
     * @return
     */
    @Override
    public boolean srem(String key, String value) {
        try{
            String path = getTasksPath(key) + Constants.SINGLE_SLASH;
            CuratorFramework zk = getZkClient();
            Stat stat = zk.checkExists().forPath(path + value);

            if(null != stat){
                zk.delete().forPath(path + value);
                logger.info("delete task:{} from tasks set ",value);
            }else{
                logger.info("delete task:{} from tasks set fail, there is no this task",value);
            }
            return true;
        }catch(Exception e){
            logger.error(String.format("delete task:" + value + " exception"),e);
            return false;
        }
    }


    /**
     * Gets all the elements of the set based on the key
     * @param key  The key is the kill/cancel queue path name
     * @return
     */
    @Override
    public Set<String> smembers(String key) {

        Set<String> tasksSet = new HashSet<>();

        try {
            List<String> list = getZkClient().getChildren().forPath(getTasksPath(key));

            for (String task : list) {
                tasksSet.add(task);
            }

            return tasksSet;
        } catch (Exception e) {
            logger.error("get all tasks from tasks queue exception",e);
        }

        return tasksSet;
    }



    /**
     * Init the task queue of zookeeper node
     */
    private void init(){
        try {
            String tasksQueuePath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
            String tasksCancelPath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_KILL);

            for(String taskQueuePath : new String[]{tasksQueuePath,tasksCancelPath}){
                if(zkClient.checkExists().forPath(taskQueuePath) == null){
                    // create a persistent parent node
                    zkClient.create().creatingParentContainersIfNeeded()
                            .withMode(CreateMode.PERSISTENT).forPath(taskQueuePath);
                    logger.info("create tasks queue parent node success : {} ",taskQueuePath);
                }
            }

        } catch (Exception e) {
            logger.error("create zk node failure",e);
        }
    }


    /**
     * Clear the task queue of zookeeper node
     * @return
     */
    @Override
    public boolean delete(){
        try {
            String tasksQueuePath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
            String tasksCancelPath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_KILL);

            for(String taskQueuePath : new String[]{tasksQueuePath,tasksCancelPath}){
                if(zkClient.checkExists().forPath(taskQueuePath) != null){

                    List<String> list = zkClient.getChildren().forPath(taskQueuePath);

                    for (String task : list) {
                        zkClient.delete().forPath(taskQueuePath + Constants.SINGLE_SLASH + task);
                        logger.info("delete task from tasks queue : {}/{} ",taskQueuePath,task);

                    }

                }
            }
            return true;
        } catch (Exception e) {
            logger.error("delete all tasks in tasks queue failure",e);
            return false;
        }
    }

    /**
     * Get the task queue path
     * @param key  task queue name
     * @return
     */
    public String getTasksPath(String key){
        return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_ROOT) + Constants.SINGLE_SLASH + key;
    }


}

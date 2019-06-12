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
import cn.escheduler.common.utils.Bytes;
import cn.escheduler.common.zk.AbstractZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A singleton of a task queue implemented with zookeeper
 * tasks queue implemention
 */
public class TaskQueueZkImpl extends AbstractZKClient implements ITaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueZkImpl.class);

    private static TaskQueueZkImpl instance;

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
    @Deprecated
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
     * @param task      ${priority}_${processInstanceId}_${taskId}
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
     * @param value    ${priority}_${processInstanceId}_${taskId}
     */
    @Override
    public void add(String key, String value) {
        try {
            String taskIdPath = getTasksPath(key) + Constants.SINGLE_SLASH + value;
            String result = getZkClient().create().withMode(CreateMode.PERSISTENT).forPath(taskIdPath, Bytes.toBytes(value));

//            String path = conf.getString(Constants.ZOOKEEPER_SCHEDULER_ROOT) + Constants.SINGLE_SLASH + Constants.SCHEDULER_TASKS_QUEUE + "_add" + Constants.SINGLE_SLASH + value;
//            getZkClient().create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,
//                    Bytes.toBytes(value));
            logger.info("add task : {} to tasks queue , result success",result);
        } catch (Exception e) {
            logger.error("add task to tasks queue exception",e);
        }

    }


    /**
     * An element pops out of the queue <p>
     * note:
     *   ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
     *   The tasks with the highest priority are selected by comparing the priorities of the above four levels from high to low.
     *
     *   流程实例优先级_流程实例id_任务优先级_任务id       high <- low
     * @param  key  task queue name
     * @param  remove  whether remove the element
     * @return the task id  to be executed
     */
    @Override
    public String poll(String key, boolean remove) {
        try{
            CuratorFramework zk = getZkClient();
            String tasksQueuePath = getTasksPath(key) + Constants.SINGLE_SLASH;
            List<String> list = zk.getChildren().forPath(getTasksPath(key));

            if(list != null && list.size() > 0){

                int size = list.size();

                String formatTargetTask = null;
                String targetTaskKey = null;
                for (int i = 0; i < size; i++) {
                    String taskDetail = list.get(i);
                    String[] taskDetailArrs = taskDetail.split(Constants.UNDERLINE);

                    if(taskDetailArrs.length == 4){
                        //format ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
                        String formatTask = String.format("%s_%010d_%s_%010d", taskDetailArrs[0], Long.parseLong(taskDetailArrs[1]), taskDetailArrs[2], Long.parseLong(taskDetailArrs[3]));
                        if(i > 0){
                            int result = formatTask.compareTo(formatTargetTask);
                            if(result < 0){
                                formatTargetTask = formatTask;
                                targetTaskKey = taskDetail;
                            }
                        }else{
                            formatTargetTask = formatTask;
                            targetTaskKey = taskDetail;
                        }
                    }else{
                        logger.error("task queue poll error, task detail :{} , please check!", taskDetail);
                    }
                }

                if(formatTargetTask != null){
                    String taskIdPath = tasksQueuePath + targetTaskKey;

                    logger.info("consume task {}", taskIdPath);

                    String[] vals = targetTaskKey.split(Constants.UNDERLINE);

                    if(remove){
                        removeNode(key, targetTaskKey);
                    }
                    logger.info("consume task: {},there still have {} tasks need to be executed", vals[vals.length - 1], size - 1);
                    return targetTaskKey;
                }else{
                    logger.error("should not go here, task queue poll error, please check!");
                }
            }

        } catch (Exception e) {
            logger.error("add task to tasks queue exception",e);
        }
        return null;
    }

    @Override
    public void removeNode(String key, String nodeValue){

        CuratorFramework zk = getZkClient();
        String tasksQueuePath = getTasksPath(key) + Constants.SINGLE_SLASH;
        String taskIdPath = tasksQueuePath + nodeValue;
        logger.info("consume task {}", taskIdPath);
        try{
            Stat stat = zk.checkExists().forPath(taskIdPath);
            if(stat != null){
                zk.delete().forPath(taskIdPath);
            }
        }catch(Exception e){
            logger.error(String.format("delete task:%s from zookeeper fail, exception:" ,nodeValue) ,e);
        }

    }



    /**
     * In order to be compatible with redis implementation
     *
     * To be compatible with the redis implementation, add an element to the set
     * @param key   The key is the kill/cancel queue path name
     * @param value host-taskId  The name of the zookeeper node
     */
    @Override
    public void sadd(String key,String value) {
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

        } catch (Exception e) {
            logger.error("add task to tasks set exception",e);
        }
    }


    /**
     * delete the value corresponding to the key in the set
     * @param key   The key is the kill/cancel queue path name
     * @param value host-taskId-taskType The name of the zookeeper node
     */
    @Override
    public void srem(String key, String value) {
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

        }catch(Exception e){
            logger.error(String.format("delete task:" + value + " exception"),e);
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
            String tasksQueuePath = getTasksPath(Constants.SCHEDULER_TASKS_QUEUE);
            String tasksCancelPath = getTasksPath(Constants.SCHEDULER_TASKS_KILL);

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
     */
    @Override
    public void delete(){
        try {
            String tasksQueuePath = getTasksPath(Constants.SCHEDULER_TASKS_QUEUE);
            String tasksCancelPath = getTasksPath(Constants.SCHEDULER_TASKS_KILL);

            for(String taskQueuePath : new String[]{tasksQueuePath,tasksCancelPath}){
                if(zkClient.checkExists().forPath(taskQueuePath) != null){

                    List<String> list = zkClient.getChildren().forPath(taskQueuePath);

                    for (String task : list) {
                        zkClient.delete().forPath(taskQueuePath + Constants.SINGLE_SLASH + task);
                        logger.info("delete task from tasks queue : {}/{} ",taskQueuePath,task);

                    }

                }
            }

        } catch (Exception e) {
            logger.error("delete all tasks in tasks queue failure",e);
        }
    }


    /**
     * get zookeeper client of CuratorFramework
     * @return
     */
    public CuratorFramework getZkClient() {
        return zkClient;
    }


    /**
     * Get the task queue path
     * @param key  task queue name
     * @return
     */
    public String getTasksPath(String key){
        return conf.getString(Constants.ZOOKEEPER_SCHEDULER_ROOT) + Constants.SINGLE_SLASH + key;
    }


}

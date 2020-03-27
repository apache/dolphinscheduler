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
package org.apache.dolphinscheduler.service.queue;


import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.IpUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.service.zk.ZookeeperOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * A singleton of a task queue implemented with zookeeper
 * tasks queue implementation
 */
@Service
public class TaskQueueZkImpl implements ITaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueZkImpl.class);

    private final ZookeeperOperator zookeeperOperator;

    @Autowired
    public TaskQueueZkImpl(ZookeeperOperator zookeeperOperator) {
        this.zookeeperOperator = zookeeperOperator;

        try {
            String tasksQueuePath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
            String tasksKillPath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_KILL);

            for (String key : new String[]{tasksQueuePath,tasksKillPath}){
                if (!zookeeperOperator.isExisted(key)){
                    zookeeperOperator.persist(key, "");
                    logger.info("create tasks queue parent node success : {}", key);
                }
            }
        } catch (Exception e) {
            logger.error("create tasks queue parent node failure", e);
        }
    }


    /**
     * get all tasks from tasks queue
     * @param key   task queue name
     * @return
     */
    @Override
    public List<String> getAllTasks(String key) {
        try {
            return zookeeperOperator.getChildrenKeys(getTasksPath(key));
        } catch (Exception e) {
            logger.error("get all tasks from tasks queue exception",e);
        }
        return Collections.emptyList();
    }

    /**
     * check if has a task
     * @param key queue name
     * @return true if has; false if not
     */
    @Override
    public boolean hasTask(String key) {
        try {
            return zookeeperOperator.hasChildren(getTasksPath(key));
        } catch (Exception e) {
            logger.error("check has task in tasks queue exception",e);
        }
        return false;
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

        return zookeeperOperator.isExisted(taskPath);

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
            zookeeperOperator.persist(taskIdPath, value);
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
            List<String> list = zookeeperOperator.getChildrenKeys(getTasksPath(key));

            if(CollectionUtils.isNotEmpty(list)){

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

                    //forward compatibility
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

                List<String> tasksList = getTasksListFromTreeSet(tasksNum, taskTreeSet);

                logger.info("consume tasks: {},there still have {} tasks need to be executed", Arrays.toString(tasksList.toArray()), size - tasksList.size());

                return tasksList;
            }else{
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            }

        } catch (Exception e) {
            logger.error("add task to tasks queue exception",e);
        }
        return Collections.emptyList();
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
        List<String> tasksList = new ArrayList<>(tasksNum);
        while(iterator.hasNext()){
            if(j++ >= tasksNum){
                break;
            }
            String task = iterator.next();
            tasksList.add(getOriginTaskFormat(task));
        }
        return tasksList;
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
    public void removeNode(String key, String nodeValue){

        String tasksQueuePath = getTasksPath(key) + Constants.SINGLE_SLASH;
        String taskIdPath = tasksQueuePath + nodeValue;
        logger.info("removeNode task {}", taskIdPath);
        try{
            zookeeperOperator.remove(taskIdPath);

        }catch(Exception e){
            logger.error("delete task:{} from zookeeper fail, exception:" ,nodeValue ,e);
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
                if(!zookeeperOperator.isExisted(path + value)){
                    zookeeperOperator.persist(path + value,value);
                    logger.info("add task:{} to tasks set ",value);
                } else{
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
            zookeeperOperator.remove(path + value);

        }catch(Exception e){
            logger.error("delete task:{} exception",value,e);
        }
    }


    /**
     * Gets all the elements of the set based on the key
     * @param key  The key is the kill/cancel queue path name
     * @return
     */
    @Override
    public Set<String> smembers(String key) {
        try {
            List<String> list = zookeeperOperator.getChildrenKeys(getTasksPath(key));
            return new HashSet<>(list);
        } catch (Exception e) {
            logger.error("get all tasks from tasks queue exception",e);
        }
        return Collections.emptySet();
    }

    /**
     * Clear the task queue of zookeeper node
     */
    @Override
    public void delete(){
        try {
            String tasksQueuePath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
            String tasksKillPath = getTasksPath(Constants.DOLPHINSCHEDULER_TASKS_KILL);

            for (String key : new String[]{tasksQueuePath,tasksKillPath}){
                if (zookeeperOperator.isExisted(key)){
                    List<String> list = zookeeperOperator.getChildrenKeys(key);
                    for (String task : list) {
                        zookeeperOperator.remove(key + Constants.SINGLE_SLASH + task);
                        logger.info("delete task from tasks queue : {}/{} ", key, task);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("delete all tasks in tasks queue failure", e);
        }
    }

    /**
     * Get the task queue path
     * @param key  task queue name
     * @return
     */
    public String getTasksPath(String key){
        return zookeeperOperator.getZookeeperConfig().getDsRoot() + Constants.SINGLE_SLASH + key;
    }

}

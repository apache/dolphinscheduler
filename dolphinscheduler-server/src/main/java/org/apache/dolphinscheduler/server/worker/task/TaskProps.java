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
package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.process.Property;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * task props
 */
public class TaskProps {

  /**
   * task node name
   **/
  private String nodeName;

  /**
   * task instance id
   **/
  private int taskInstId;

  /**
   * tenant code , execute task linux user
   **/
  private String tenantCode;

  /**
   * task type
   */
  private String taskType;

  /**
   * task parameters
   **/
  private String taskParams;

  /**
   * task dir
   **/
  private String taskDir;

  /**
   * queue
   **/
  private String queue;

  /**
   * env file
   **/
  private String envFile;

  /**
   * defined params
   **/
  private Map<String, String> definedParams;

  /**
   * task app id
   */
  private String taskAppId;

  /**
   * task start time
   */
  private Date taskStartTime;

  /**
   * task timeout
   */
  private int taskTimeout;

  /**
   * task timeout strategy
   */
  private TaskTimeoutStrategy taskTimeoutStrategy;
  /**
   * task dependence
   */
  private String dependence;

  /**
   * schedule time
   */
  private Date scheduleTime;

  /**
   *  command type is complement
   */
  private CommandType cmdTypeIfComplement;

  /**
   * constructor
   */
  public TaskProps(){}

  /**
   * constructor
   * @param taskParams          task params
   * @param taskDir             task dir
   * @param scheduleTime        schedule time
   * @param nodeName            node name
   * @param taskType            task type
   * @param taskInstId          task instance id
   * @param envFile             env file
   * @param tenantCode          tenant code
   * @param queue               queue
   * @param taskStartTime       task start time
   * @param definedParams       defined params
   * @param dependence          dependence
   * @param cmdTypeIfComplement cmd type if complement
   */
  public TaskProps(String taskParams,
                   String taskDir,
                   Date scheduleTime,
                   String nodeName,
                   String taskType,
                   int taskInstId,
                   String envFile,
                   String tenantCode,
                   String queue,
                   Date taskStartTime,
                   Map<String, String> definedParams,
                   String dependence,
                   CommandType cmdTypeIfComplement){
    this.taskParams = taskParams;
    this.taskDir = taskDir;
    this.scheduleTime = scheduleTime;
    this.nodeName = nodeName;
    this.taskType = taskType;
    this.taskInstId = taskInstId;
    this.envFile = envFile;
    this.tenantCode = tenantCode;
    this.queue = queue;
    this.taskStartTime = taskStartTime;
    this.definedParams = definedParams;
    this.dependence = dependence;
    this.cmdTypeIfComplement = cmdTypeIfComplement;

  }

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public String getTaskParams() {
    return taskParams;
  }

  public void setTaskParams(String taskParams) {
    this.taskParams = taskParams;
  }

  public String getTaskDir() {
    return taskDir;
  }

  public void setTaskDir(String taskDir) {
    this.taskDir = taskDir;
  }

  public Map<String, String> getDefinedParams() {
    return definedParams;
  }

  public void setDefinedParams(Map<String, String> definedParams) {
    this.definedParams = definedParams;
  }

  public String getEnvFile() {
    return envFile;
  }

  public void setEnvFile(String envFile) {
    this.envFile = envFile;
  }


  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public int getTaskInstId() {
    return taskInstId;
  }

  public void setTaskInstId(int taskInstId) {
    this.taskInstId = taskInstId;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }


  public String getTaskAppId() {
    return taskAppId;
  }

  public void setTaskAppId(String taskAppId) {
    this.taskAppId = taskAppId;
  }

  public Date getTaskStartTime() {
    return taskStartTime;
  }

  public void setTaskStartTime(Date taskStartTime) {
    this.taskStartTime = taskStartTime;
  }

  public int getTaskTimeout() {
    return taskTimeout;
  }

  public void setTaskTimeout(int taskTimeout) {
    this.taskTimeout = taskTimeout;
  }

  public TaskTimeoutStrategy getTaskTimeoutStrategy() {
    return taskTimeoutStrategy;
  }

  public void setTaskTimeoutStrategy(TaskTimeoutStrategy taskTimeoutStrategy) {
    this.taskTimeoutStrategy = taskTimeoutStrategy;
  }

  public String getTaskType() {
    return taskType;
  }

  public void setTaskType(String taskType) {
    this.taskType = taskType;
  }

  public String getDependence() {
    return dependence;
  }

  public void setDependence(String dependence) {
    this.dependence = dependence;
  }

  public Date getScheduleTime() {
    return scheduleTime;
  }

  public void setScheduleTime(Date scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  public CommandType getCmdTypeIfComplement() {
    return cmdTypeIfComplement;
  }

  public void setCmdTypeIfComplement(CommandType cmdTypeIfComplement) {
    this.cmdTypeIfComplement = cmdTypeIfComplement;
  }

  /**
   * get parameters map
   * @return user defined params map
   */
  public Map<String,Property> getUserDefParamsMap() {
    if (definedParams != null) {
      Map<String,Property> userDefParamsMaps = new HashMap<>();
      Iterator<Map.Entry<String, String>> iter = definedParams.entrySet().iterator();
      while (iter.hasNext()){
        Map.Entry<String, String> en = iter.next();
        Property property = new Property(en.getKey(), Direct.IN, DataType.VARCHAR , en.getValue());
        userDefParamsMaps.put(property.getProp(),property);
      }
      return userDefParamsMaps;
    }
    return null;
  }
}

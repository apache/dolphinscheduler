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
package cn.escheduler.server.worker.task;

import cn.escheduler.common.enums.DataType;
import cn.escheduler.common.enums.Direct;
import cn.escheduler.common.enums.TaskTimeoutStrategy;
import cn.escheduler.common.process.Property;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  task props
 */
public class TaskProps {

  /**
   * task node name
   **/
  private String nodeName;

  /**
   *  task instance id
   **/
  private int taskInstId;

  /**
   *  tenant code , execute task linux user
   **/
  private String tenantCode;

  /**
   *  task parameters
   **/
  private String taskParams;

  /**
   *  task dir
   **/
  private String taskDir;

  /**
   *  queue
   **/
  private String queue;

  /**
   *  env file
   **/
  private String envFile;

  /**
   *  defined params
   **/
  private Map<String, String> definedParams;

  /**
   *  task path
   */
  private String taskAppId;

  /**
   * task start time
   */
  private Date taskStartTime;

  /**
   *  task timeout
   */
  private int taskTimeout;

  /**
   *  task timeout strategy
   */
  private TaskTimeoutStrategy taskTimeoutStrategy;
  /**
   * task dependence
   */
  private String dependence;

  /**
   * schedule time
   * @return
   */
  private Date scheduleTime;


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

  /**
   *  get parameters map
   * @return
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
}

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
  private String taskName;

  /**
   * task instance id
   **/
  private int taskInstanceId;

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
   *  host
   */
  private String host;

  /**
   *  log path
   */
  private String logPath;

  /**
   * execute path
   */
  private String executePath;

  /**
   * constructor
   */
  public TaskProps(){}

  /**
   * constructor
   * @param taskParams taskParams
   * @param scheduleTime scheduleTime
   * @param nodeName nodeName
   * @param taskType taskType
   * @param taskInstanceId taskInstanceId
   * @param envFile envFile
   * @param tenantCode tenantCode
   * @param queue queue
   * @param taskStartTime taskStartTime
   * @param definedParams definedParams
   * @param dependence dependence
   * @param cmdTypeIfComplement cmdTypeIfComplement
   * @param host host
   * @param logPath logPath
   * @param executePath executePath
   */
  public TaskProps(String taskParams,
                   Date scheduleTime,
                   String nodeName,
                   String taskType,
                   int taskInstanceId,
                   String envFile,
                   String tenantCode,
                   String queue,
                   Date taskStartTime,
                   Map<String, String> definedParams,
                   String dependence,
                   CommandType cmdTypeIfComplement,
                   String host,
                   String logPath,
                   String executePath){
    this.taskParams = taskParams;
    this.scheduleTime = scheduleTime;
    this.taskName = nodeName;
    this.taskType = taskType;
    this.taskInstanceId = taskInstanceId;
    this.envFile = envFile;
    this.tenantCode = tenantCode;
    this.queue = queue;
    this.taskStartTime = taskStartTime;
    this.definedParams = definedParams;
    this.dependence = dependence;
    this.cmdTypeIfComplement = cmdTypeIfComplement;
    this.host = host;
    this.logPath = logPath;
    this.executePath = executePath;
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

  public String getExecutePath() {
    return executePath;
  }

  public void setExecutePath(String executePath) {
    this.executePath = executePath;
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


  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public int getTaskInstanceId() {
    return taskInstanceId;
  }

  public void setTaskInstanceId(int taskInstanceId) {
    this.taskInstanceId = taskInstanceId;
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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getLogPath() {
    return logPath;
  }

  public void setLogPath(String logPath) {
    this.logPath = logPath;
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

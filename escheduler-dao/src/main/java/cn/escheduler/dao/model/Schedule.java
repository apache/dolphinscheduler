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
package cn.escheduler.dao.model;

import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.enums.WarningType;

import java.util.Date;

/**
 * schedule
 *
 */
public class Schedule {

  private int id;
  /**
   * process definition id
   */
  private int processDefinitionId;

  /**
   * process definition name
   */
  private String processDefinitionName;

  /**
   * project name
   */
  private String projectName;

  /**
   * schedule description
   */
  private String desc;

  /**
   * schedule start time
   */
  private Date startTime;

  /**
   * schedule end time
   */
  private Date endTime;

  /**
   * crontab expression
   */
  private String crontab;

  /**
   * failure strategy
   */
  private FailureStrategy failureStrategy;

  /**
   * warning type
   */
  private WarningType warningType;

  /**
   * create time
   */
  private Date createTime;

  /**
   * update time
   */
  private Date updateTime;

  /**
   * created user id
   */
  private int userId;

  /**
   * created user name
   */
  private String userName;

  /**
   * release state
   */
  private ReleaseState releaseState;

  /**
   * warning group id
   */
  private int warningGroupId;


  /**
   * process instance priority
   */
  private Priority processInstancePriority;

  /**
   *  worker group id
   */
  private int workerGroupId;

  public int getWarningGroupId() {
    return warningGroupId;
  }

  public void setWarningGroupId(int warningGroupId) {
    this.warningGroupId = warningGroupId;
  }



  public Schedule() {
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }



  public Date getStartTime() {

    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getCrontab() {
    return crontab;
  }

  public void setCrontab(String crontab) {
    this.crontab = crontab;
  }

  public FailureStrategy getFailureStrategy() {
    return failureStrategy;
  }

  public void setFailureStrategy(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  public WarningType getWarningType() {
    return warningType;
  }

  public void setWarningType(WarningType warningType) {
    this.warningType = warningType;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }


  public ReleaseState getReleaseState() {
    return releaseState;
  }

  public void setReleaseState(ReleaseState releaseState) {
    this.releaseState = releaseState;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public int getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(int processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Priority getProcessInstancePriority() {
    return processInstancePriority;
  }

  public void setProcessInstancePriority(Priority processInstancePriority) {
    this.processInstancePriority = processInstancePriority;
  }


  public int getWorkerGroupId() {
    return workerGroupId;
  }

  public void setWorkerGroupId(int workerGroupId) {
    this.workerGroupId = workerGroupId;
  }

  @Override
  public String toString() {
    return "Schedule{" +
            "id=" + id +
            ", processDefinitionId=" + processDefinitionId +
            ", processDefinitionName='" + processDefinitionName + '\'' +
            ", projectName='" + projectName + '\'' +
            ", desc='" + desc + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", crontab='" + crontab + '\'' +
            ", failureStrategy=" + failureStrategy +
            ", warningType=" + warningType +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", userId=" + userId +
            ", userName='" + userName + '\'' +
            ", releaseState=" + releaseState +
            ", warningGroupId=" + warningGroupId +
            ", processInstancePriority=" + processInstancePriority +
            ", workerGroupId=" + workerGroupId +
            '}';
  }

}

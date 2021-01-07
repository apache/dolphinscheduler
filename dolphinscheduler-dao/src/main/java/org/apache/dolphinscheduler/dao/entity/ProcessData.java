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
package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import java.util.List;

/**
 * definition json data structure
 */
public class ProcessData {
  /**
   * task list
   */
  private List<TaskNode> tasks;

  /**
   * global parameters
   */
  private List<Property> globalParams;


  private int timeout;

  private int tenantId;


  public ProcessData() {
  }

  /**
   *
   * @param tasks tasks
   * @param globalParams globalParams
   */
  public ProcessData(List<TaskNode> tasks, List<Property> globalParams) {
    this.tasks = tasks;
    this.globalParams = globalParams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ProcessData that = (ProcessData) o;

    return CollectionUtils.equalLists(tasks, that.tasks) &&
        CollectionUtils.equalLists(globalParams, that.globalParams);
  }

  public List<TaskNode> getTasks() {
    return tasks;
  }

  public void setTasks(List<TaskNode> tasks) {
    this.tasks = tasks;
  }

  public List<Property> getGlobalParams() {
    return globalParams;
  }

  public void setGlobalParams(List<Property> globalParams) {
    this.globalParams = globalParams;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getTenantId() {
    return tenantId;
  }

  public void setTenantId(int tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public String toString() {
    return "ProcessData{" +
            "tasks=" + tasks +
            ", globalParams=" + globalParams +
            ", timeout=" + timeout +
            ", tenantId=" + tenantId +
            '}';
  }
}

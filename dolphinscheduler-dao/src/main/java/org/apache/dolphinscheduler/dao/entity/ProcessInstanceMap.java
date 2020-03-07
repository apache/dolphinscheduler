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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * process instance map
 */
@TableName("t_ds_relation_process_instance")
public class ProcessInstanceMap {

    /**
     * id
     */
    @TableId(value="id", type=IdType.AUTO)
    private int id;

    /**
     * parent process instance id
     */
    private int parentProcessInstanceId;

    /**
     * parent task instance id
     */
    private int parentTaskInstanceId;

    /**
     * process instance id
     */
    private int processInstanceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(int parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    public int getParentTaskInstanceId() {
        return parentTaskInstanceId;
    }

    public void setParentTaskInstanceId(int parentTaskInstanceId) {
        this.parentTaskInstanceId = parentTaskInstanceId;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String toString() {
        return "ProcessInstanceMap{" +
                "id=" + id +
                ", parentProcessInstanceId=" + parentProcessInstanceId +
                ", parentTaskInstanceId=" + parentTaskInstanceId +
                ", processInstanceId=" + processInstanceId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInstanceMap that = (ProcessInstanceMap) o;

        if (id != that.id) return false;
        if (parentProcessInstanceId != that.parentProcessInstanceId) return false;
        if (parentTaskInstanceId != that.parentTaskInstanceId) return false;
        return processInstanceId == that.processInstanceId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + parentProcessInstanceId;
        result = 31 * result + parentTaskInstanceId;
        result = 31 * result + processInstanceId;
        return result;
    }
}

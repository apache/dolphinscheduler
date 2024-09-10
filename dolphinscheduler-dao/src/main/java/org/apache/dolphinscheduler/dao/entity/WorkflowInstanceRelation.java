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

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_relation_workflow_instance")
public class WorkflowInstanceRelation {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private int parentWorkflowInstanceId;

    private int parentTaskInstanceId;

    private int workflowInstanceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkflowInstanceRelation that = (WorkflowInstanceRelation) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }
        if (parentWorkflowInstanceId != that.parentWorkflowInstanceId) {
            return false;
        }
        if (parentTaskInstanceId != that.parentTaskInstanceId) {
            return false;
        }
        return workflowInstanceId == that.workflowInstanceId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + parentWorkflowInstanceId;
        result = 31 * result + parentTaskInstanceId;
        result = 31 * result + workflowInstanceId;
        return result;
    }

}

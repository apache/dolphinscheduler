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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * workflow instance map mapper interface
 */
public interface WorkflowInstanceRelationMapper extends BaseMapper<WorkflowInstanceRelation> {

    /**
     * query workflow instance by parentId
     * @param parentWorkflowInstanceId parentWorkflowInstanceId
     * @param parentTaskId parentTaskId
     * @return workflow instance map
     */
    WorkflowInstanceRelation queryByParentId(@Param("parentWorkflowInstanceId") int parentWorkflowInstanceId,
                                             @Param("parentTaskId") int parentTaskId);

    /**
     * query by sub process id
     * @param subWorkflowInstanceId subWorkflowInstanceId
     * @return workflow instance map
     */
    WorkflowInstanceRelation queryBySubWorkflowId(@Param("subWorkflowInstanceId") Integer subWorkflowInstanceId);

    /**
     * delete by parent process id
     * @param parentWorkflowInstanceId parentWorkflowInstanceId
     * @return delete result
     */
    int deleteByParentWorkflowInstanceId(@Param("parentWorkflowInstanceId") int parentWorkflowInstanceId);

    /**
     *  query sub workflow instance  ids by parent instance id
     * @param parentInstanceId parentInstanceId
     * @return sub workflow instance ids
     */
    List<Integer> querySubIdListByParentId(@Param("parentInstanceId") int parentInstanceId);

    void deleteByParentId(@Param("parentWorkflowInstanceId") int workflowInstanceId);
}

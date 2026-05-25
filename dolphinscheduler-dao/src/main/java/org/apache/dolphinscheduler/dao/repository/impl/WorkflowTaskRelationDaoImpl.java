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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

@Repository
public class WorkflowTaskRelationDaoImpl extends BaseDao<WorkflowTaskRelation, WorkflowTaskRelationMapper>
        implements
            WorkflowTaskRelationDao {

    public WorkflowTaskRelationDaoImpl(@NonNull WorkflowTaskRelationMapper workflowTaskRelationMapper) {
        super(workflowTaskRelationMapper);
    }

    @Override
    public List<WorkflowTaskRelation> queryByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return mybatisMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
    }

    @Override
    public int batchInsert(List<WorkflowTaskRelation> taskRelationList) {
        return mybatisMapper.batchInsert(taskRelationList);
    }

    @Override
    public List<WorkflowTaskRelation> queryUpstreamByCode(long projectCode, long taskCode) {
        return mybatisMapper.queryUpstreamByCode(projectCode, taskCode);
    }

    @Override
    public List<WorkflowTaskRelation> queryUpstreamByCodes(long projectCode, long taskCode, Long[] preTaskCodes) {
        return mybatisMapper.queryUpstreamByCodes(projectCode, taskCode, preTaskCodes);
    }

    @Override
    public List<WorkflowTaskRelation> queryWorkflowTaskRelationsByWorkflowDefinitionCode(long workflowDefinitionCode,
                                                                                         Integer workflowDefinitionVersion) {
        return mybatisMapper.queryWorkflowTaskRelationsByWorkflowDefinitionCode(workflowDefinitionCode,
                workflowDefinitionVersion);
    }

    @Override
    public List<WorkflowTaskRelation> queryDownstreamByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return mybatisMapper.queryDownstreamByWorkflowDefinitionCode(workflowDefinitionCode);
    }

    @Override
    public boolean updateWorkflowTaskRelationTaskVersion(WorkflowTaskRelation workflowTaskRelation) {
        return mybatisMapper.updateWorkflowTaskRelationTaskVersion(workflowTaskRelation) > 0;
    }

    @Override
    public void deleteByWorkflowDefinitionCodeAndVersion(long workflowDefinitionCode, int workflowDefinitionVersion) {
        mybatisMapper.deleteByWorkflowDefinitionCodeAndVersion(workflowDefinitionCode, workflowDefinitionVersion);
    }

    @Override
    public List<WorkflowTaskRelation> queryWorkflowTaskRelationByTaskCodeAndTaskVersion(long taskCode,
                                                                                        long postTaskVersion) {
        return mybatisMapper.queryWorkflowTaskRelationByTaskCodeAndTaskVersion(taskCode, postTaskVersion);
    }

    @Override
    public List<WorkflowTaskRelation> queryByCode(long projectCode, long workflowDefinitionCode, long preTaskCode,
                                                  long postTaskCode) {
        return mybatisMapper.queryByCode(projectCode, workflowDefinitionCode, preTaskCode, postTaskCode);
    }
}

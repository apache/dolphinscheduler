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

import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskLineageMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskLineageDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

@Repository
public class WorkflowTaskLineageDaoImpl extends BaseDao<WorkflowTaskLineage, WorkflowTaskLineageMapper>
        implements
            WorkflowTaskLineageDao {

    public WorkflowTaskLineageDaoImpl(@NonNull WorkflowTaskLineageMapper workflowTaskLineageMapper) {
        super(workflowTaskLineageMapper);
    }

    @Override
    public int batchDeleteByWorkflowDefinitionCode(List<Long> workflowDefinitionCodes) {
        if (CollectionUtils.isEmpty(workflowDefinitionCodes)) {
            return 0;
        }
        return mybatisMapper.batchDeleteByWorkflowDefinitionCode(workflowDefinitionCodes);
    }

    @Override
    public int batchInsert(List<WorkflowTaskLineage> workflowTaskLineages) {
        if (CollectionUtils.isEmpty(workflowTaskLineages)) {
            return 0;
        }
        return mybatisMapper.batchInsert(workflowTaskLineages);
    }

    @Override
    public List<WorkflowTaskLineage> queryByProjectCode(long projectCode) {
        return mybatisMapper.queryByProjectCode(projectCode);
    }

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByCode(long workflowDefinitionCode) {
        return mybatisMapper.queryWorkFlowLineageByCode(workflowDefinitionCode);
    }

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String workflowDefinitionName) {
        return mybatisMapper.queryWorkFlowLineageByName(projectCode, workflowDefinitionName);
    }

    @Override
    public List<WorkflowTaskLineage> queryWorkFlowLineageByDept(long deptProjectCode, long deptWorkflowDefinitionCode,
                                                                long deptTaskDefinitionCode) {
        return mybatisMapper.queryWorkFlowLineageByDept(deptProjectCode, deptWorkflowDefinitionCode,
                deptTaskDefinitionCode);
    }

    @Override
    public List<WorkflowTaskLineage> queryByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return mybatisMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
    }

    @Override
    public int updateWorkflowTaskLineage(List<WorkflowTaskLineage> workflowTaskLineages) {
        if (CollectionUtils.isEmpty(workflowTaskLineages)) {
            return 0;
        }
        this.batchDeleteByWorkflowDefinitionCode(
                workflowTaskLineages.stream().map(WorkflowTaskLineage::getWorkflowDefinitionCode)
                        .distinct().collect(Collectors.toList()));
        return mybatisMapper.batchInsert(workflowTaskLineages);
    }
}

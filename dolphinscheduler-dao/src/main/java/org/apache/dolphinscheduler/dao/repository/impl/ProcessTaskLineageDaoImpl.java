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

import lombok.NonNull;

import org.apache.dolphinscheduler.dao.entity.ProcessTaskLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskLineageMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskLineageDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class ProcessTaskLineageDaoImpl extends BaseDao<ProcessTaskLineage, ProcessTaskLineageMapper>
    implements ProcessTaskLineageDao {

    public ProcessTaskLineageDaoImpl(@NonNull ProcessTaskLineageMapper processTaskLineageMapper) {
        super(processTaskLineageMapper);
    }

    @Override
    public int batchDeleteByProcessDefinitionCode(List<Long> processDefinitionCodes) {
        if (CollectionUtils.isEmpty(processDefinitionCodes)) {
            return 0;
        }
        return mybatisMapper.batchDeleteByProcessDefinitionCode(processDefinitionCodes);
    }

    @Override
    public int batchInsert(List<ProcessTaskLineage> processTaskLineages) {
        if (CollectionUtils.isEmpty(processTaskLineages)) {
            return 0;
        }
        return mybatisMapper.batchInsert(processTaskLineages);
    }

    @Override
    public List<ProcessTaskLineage> queryByProjectCode(long projectCode) {
        return mybatisMapper.queryByProjectCode(projectCode);
    }

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByCode(long processDefinitionCode) {
        return mybatisMapper.queryWorkFlowLineageByCode(processDefinitionCode);
    }

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String processDefinitionName) {
        return mybatisMapper.queryWorkFlowLineageByName(projectCode, processDefinitionName);
    }

    @Override
    public List<ProcessTaskLineage> queryWorkFlowLineageByDept(long deptProjectCode, long deptProcessDefinitionCode, long deptTaskDefinitionCode) {
        return mybatisMapper.queryWorkFlowLineageByDept(deptProjectCode, deptProcessDefinitionCode, deptTaskDefinitionCode);
    }

    @Override
    public List<ProcessTaskLineage> queryByProcessDefinitionCode(long processDefinitionCode) {
        return mybatisMapper.queryByProcessDefinitionCode(processDefinitionCode);
    }

    @Override
    public void truncateTable() {
        mybatisMapper.truncateTable();
    }

    @Override
    public int updateProcessTaskLineage(List<ProcessTaskLineage> processTaskLineages) {
        if (CollectionUtils.isEmpty(processTaskLineages)) {
            return 0;
        }
        this.batchDeleteByProcessDefinitionCode(processTaskLineages.stream().map(ProcessTaskLineage::getProcessDefinitionCode)
            .distinct().collect(Collectors.toList()));
        return mybatisMapper.batchInsert(processTaskLineages);
    }
}

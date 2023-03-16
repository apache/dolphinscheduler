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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
public class ProcessDefinitionDaoImpl implements ProcessDefinitionDao {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Override
    public PageListingResult<ProcessDefinition> listingProcessDefinition(int pageNumber, int pageSize, String searchVal,
                                                                         int userId, long projectCode) {
        Page<ProcessDefinition> page = new Page<>(pageNumber, pageSize);
        IPage<ProcessDefinition> processDefinitions =
                processDefinitionMapper.queryDefineListPaging(page, searchVal, userId, projectCode);

        return PageListingResult.<ProcessDefinition>builder()
                .totalCount(processDefinitions.getTotal())
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .records(processDefinitions.getRecords())
                .build();
    }

    @Override
    public List<ProcessDefinition> queryProcessDefinitionsByCodesAndVersions(List<ProcessInstance> processInstances) {
        if (Objects.isNull(processInstances) || processInstances.isEmpty()) {
            return new ArrayList<>();
        }
        List<ProcessDefinitionLog> processDefinitionLogs = processInstances
                .parallelStream()
                .map(processInstance -> {
                    ProcessDefinitionLog processDefinitionLog = processDefinitionLogMapper
                            .queryByDefinitionCodeAndVersion(processInstance.getProcessDefinitionCode(),
                                    processInstance.getProcessDefinitionVersion());
                    return processDefinitionLog;
                })
                .collect(Collectors.toList());

        List<ProcessDefinition> processDefinitions =
                processDefinitionLogs.stream().map(log -> (ProcessDefinition) log).collect(Collectors.toList());

        return processDefinitions;
    }

    @Override
    public Optional<ProcessDefinition> queryByCode(long code) {
        return Optional.ofNullable(
                processDefinitionMapper.queryByCode(code));
    }

    @Override
    public void deleteById(Integer workflowDefinitionId) {
        processDefinitionMapper.deleteById(workflowDefinitionId);
    }

    @Override
    public void deleteByWorkflowDefinitionCode(long workflowDefinitionCode) {
        processDefinitionMapper.deleteByCode(workflowDefinitionCode);
    }
}

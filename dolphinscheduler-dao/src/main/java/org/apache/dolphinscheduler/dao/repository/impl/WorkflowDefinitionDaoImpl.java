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

import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
public class WorkflowDefinitionDaoImpl extends BaseDao<WorkflowDefinition, WorkflowDefinitionMapper>
        implements
            WorkflowDefinitionDao {

    public WorkflowDefinitionDaoImpl(@NonNull WorkflowDefinitionMapper workflowDefinitionMapper) {
        super(workflowDefinitionMapper);
    }

    @Override
    public PageListingResult<WorkflowDefinition> listingWorkflowDefinition(int pageNumber, int pageSize,
                                                                           String searchVal,
                                                                           int userId, long projectCode) {
        Page<WorkflowDefinition> page = new Page<>(pageNumber, pageSize);
        IPage<WorkflowDefinition> processDefinitions =
                mybatisMapper.queryDefineListPaging(page, searchVal, userId, projectCode);

        return PageListingResult.<WorkflowDefinition>builder()
                .totalCount(processDefinitions.getTotal())
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .records(processDefinitions.getRecords())
                .build();
    }

    @Override
    public Optional<WorkflowDefinition> queryByCode(long code) {
        return Optional.ofNullable(mybatisMapper.queryByCode(code));
    }

    @Override
    public void deleteByWorkflowDefinitionCode(long workflowDefinitionCode) {
        mybatisMapper.deleteByCode(workflowDefinitionCode);
    }

    @Override
    public List<WorkflowDefinition> queryByCodes(Collection<Long> workflowDefinitionCodes) {
        if (CollectionUtils.isEmpty(workflowDefinitionCodes)) {
            return Collections.emptyList();
        }
        return mybatisMapper.queryByCodes(workflowDefinitionCodes);
    }
}

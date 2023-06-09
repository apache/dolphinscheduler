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
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;

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
public class ProcessDefinitionDaoImpl extends BaseDao<ProcessDefinition, ProcessDefinitionMapper>
        implements
            ProcessDefinitionDao {

    public ProcessDefinitionDaoImpl(@NonNull ProcessDefinitionMapper processDefinitionMapper) {
        super(processDefinitionMapper);
    }

    @Override
    public PageListingResult<ProcessDefinition> listingProcessDefinition(int pageNumber, int pageSize, String searchVal,
                                                                         int userId, long projectCode) {
        Page<ProcessDefinition> page = new Page<>(pageNumber, pageSize);
        IPage<ProcessDefinition> processDefinitions =
                mybatisMapper.queryDefineListPaging(page, searchVal, userId, projectCode);

        return PageListingResult.<ProcessDefinition>builder()
                .totalCount(processDefinitions.getTotal())
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .records(processDefinitions.getRecords())
                .build();
    }

    @Override
    public Optional<ProcessDefinition> queryByCode(long code) {
        return Optional.ofNullable(mybatisMapper.queryByCode(code));
    }

    @Override
    public void deleteByWorkflowDefinitionCode(long workflowDefinitionCode) {
        mybatisMapper.deleteByCode(workflowDefinitionCode);
    }

    @Override
    public List<ProcessDefinition> queryByCodes(Collection<Long> processDefinitionCodes) {
        if (CollectionUtils.isEmpty(processDefinitionCodes)) {
            return Collections.emptyList();
        }
        return mybatisMapper.queryByCodes(processDefinitionCodes);
    }
}

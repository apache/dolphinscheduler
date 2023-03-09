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

import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceMapDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Process Instance Map Dao implementation
 */
@Repository
public class ProcessInstanceMapDaoImpl implements ProcessInstanceMapDao {

    @Autowired
    private ProcessInstanceMapMapper processInstanceMapMapper;

    @Override
    public int updateWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap) {
        return processInstanceMapMapper.updateById(processInstanceMap);
    }

    @Override
    public int createWorkProcessInstanceMap(@NonNull ProcessInstanceMap processInstanceMap) {
        return processInstanceMapMapper.insert(processInstanceMap);
    }

    @Override
    public ProcessInstanceMap findWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId) {
        return processInstanceMapMapper.queryByParentId(parentWorkProcessId, parentTaskId);
    }

    @Override
    public List<Integer> querySubWorkflowInstanceIds(int workflowInstanceId) {
        return processInstanceMapMapper.querySubIdListByParentId(workflowInstanceId);
    }

    @Override
    public void deleteByParentId(int workflowInstanceId) {
        processInstanceMapMapper.deleteByParentId(workflowInstanceId);
    }
}

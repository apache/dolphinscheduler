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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.service.EnvironmentWorkerGroupRelationService;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * environment worker group relation service impl
 */
@Service
@Slf4j
public class EnvironmentWorkerGroupRelationServiceImpl extends BaseServiceImpl
        implements
            EnvironmentWorkerGroupRelationService {

    @Autowired
    private EnvironmentWorkerGroupRelationMapper environmentWorkerGroupRelationMapper;

    /**
     * query environment worker group relation
     *
     * @param environmentCode environment code
     */
    @Override
    public List<EnvironmentWorkerGroupRelation> queryEnvironmentWorkerGroupRelation(Long environmentCode) {
        return environmentWorkerGroupRelationMapper.queryByEnvironmentCode(environmentCode);
    }

    /**
     * query all environment worker group relation
     *
     * @return all relation list
     */
    @Override
    public List<EnvironmentWorkerGroupRelation> queryAllEnvironmentWorkerGroupRelationList() {
        return environmentWorkerGroupRelationMapper.selectList(null);
    }
}

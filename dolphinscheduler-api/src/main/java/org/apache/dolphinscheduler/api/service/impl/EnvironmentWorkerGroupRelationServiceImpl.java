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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.EnvironmentWorkerGroupRelationService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * task definition service impl
 */
@Service
public class EnvironmentWorkerGroupRelationServiceImpl extends BaseServiceImpl
        implements
            EnvironmentWorkerGroupRelationService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentWorkerGroupRelationServiceImpl.class);

    @Autowired
    private EnvironmentWorkerGroupRelationMapper environmentWorkerGroupRelationMapper;

    /**
     * query environment worker group relation
     *
     * @param environmentCode environment code
     */
    @Override
    public Map<String, Object> queryEnvironmentWorkerGroupRelation(Long environmentCode) {
        Map<String, Object> result = new HashMap<>();
        List<EnvironmentWorkerGroupRelation> relations =
                environmentWorkerGroupRelationMapper.queryByEnvironmentCode(environmentCode);
        result.put(Constants.DATA_LIST, relations);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all environment worker group relation
     *
     * @return all relation list
     */
    @Override
    public Map<String, Object> queryAllEnvironmentWorkerGroupRelationList() {
        Map<String, Object> result = new HashMap<>();

        List<EnvironmentWorkerGroupRelation> relations = environmentWorkerGroupRelationMapper.selectList(null);

        result.put(Constants.DATA_LIST, relations);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}

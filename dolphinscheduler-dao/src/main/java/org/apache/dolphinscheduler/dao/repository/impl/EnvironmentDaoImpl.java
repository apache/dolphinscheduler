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

import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;

import java.util.Optional;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

@Repository
public class EnvironmentDaoImpl extends BaseDao<Environment, EnvironmentMapper> implements IEnvironmentDao {

    public EnvironmentDaoImpl(@NonNull EnvironmentMapper environmentMapper) {
        super(environmentMapper);
    }

    @Override
    public Optional<Environment> queryByEnvironmentCode(Long environmentCode) {
        return Optional.ofNullable(mybatisMapper.queryByEnvironmentCode(environmentCode));
    }
}

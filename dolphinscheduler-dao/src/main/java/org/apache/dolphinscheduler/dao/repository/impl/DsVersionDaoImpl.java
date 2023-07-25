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

import org.apache.dolphinscheduler.dao.entity.DsVersion;
import org.apache.dolphinscheduler.dao.mapper.DsVersionMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.DsVersionDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class DsVersionDaoImpl extends BaseDao<DsVersion, DsVersionMapper> implements DsVersionDao {

    public DsVersionDaoImpl(@NonNull DsVersionMapper dsVersionMapper) {
        super(dsVersionMapper);
    }

    @Override
    public Optional<DsVersion> selectVersion() {
        List<DsVersion> dsVersions = mybatisMapper.selectList(null);
        if (CollectionUtils.isEmpty(dsVersions)) {
            log.info("There is no version information in the database");
        }
        if (dsVersions.size() > 1) {
            log.info("There are multiple version information in the database");
        }
        return dsVersions.stream().findFirst();
    }
}

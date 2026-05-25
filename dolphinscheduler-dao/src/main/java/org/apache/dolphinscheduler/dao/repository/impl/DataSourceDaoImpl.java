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

import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;

import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
public class DataSourceDaoImpl extends BaseDao<DataSource, DataSourceMapper> implements DataSourceDao {

    public DataSourceDaoImpl(@NonNull DataSourceMapper dataSourceMapper) {
        super(dataSourceMapper);
    }

    @Override
    public List<DataSource> queryDataSourceByType(int userId, Integer type) {
        return mybatisMapper.queryDataSourceByType(userId, type);
    }

    @Override
    public IPage<DataSource> queryDataSourcePaging(IPage<DataSource> page, int userId, String name) {
        return mybatisMapper.selectPaging(page, userId, name);
    }

    @Override
    public List<DataSource> queryDataSourceByName(String name) {
        return mybatisMapper.queryDataSourceByName(name);
    }

    @Override
    public List<DataSource> queryAuthedDatasource(int userId) {
        return mybatisMapper.queryAuthedDatasource(userId);
    }

    @Override
    public List<DataSource> queryDatasourceExceptUserId(int userId) {
        return mybatisMapper.queryDatasourceExceptUserId(userId);
    }

    @Override
    public <T> List<DataSource> listAuthorizedDataSource(int userId, T[] dataSourceIds) {
        return mybatisMapper.listAuthorizedDataSource(userId, dataSourceIds);
    }

    @Override
    public IPage<DataSource> queryDataSourcePagingByIds(Page<DataSource> dataSourcePage,
                                                        List<Integer> dataSourceIds,
                                                        String name) {
        return mybatisMapper.selectPagingByIds(dataSourcePage, dataSourceIds, name);
    }

    @Override
    public List<DataSource> queryByUserId(int userId) {
        return mybatisMapper.selectByMap(Collections.singletonMap("user_id", userId));
    }
}

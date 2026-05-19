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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DataSource;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface DataSourceDao extends IDao<DataSource> {

    List<DataSource> queryDataSourceByType(int userId, Integer type);

    IPage<DataSource> queryDataSourcePaging(IPage<DataSource> page, int userId, String name);

    List<DataSource> queryDataSourceByName(String name);

    List<DataSource> queryAuthedDatasource(int userId);

    List<DataSource> queryDatasourceExceptUserId(int userId);

    <T> List<DataSource> listAuthorizedDataSource(int userId, T[] dataSourceIds);

    IPage<DataSource> queryDataSourcePagingByIds(Page<DataSource> dataSourcePage,
                                                 List<Integer> dataSourceIds,
                                                 String name);

    List<DataSource> queryByUserId(int userId);
}

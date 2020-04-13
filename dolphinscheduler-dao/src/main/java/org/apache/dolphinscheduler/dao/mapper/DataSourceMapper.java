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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.DataSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * datasource mapper interface
 */
public interface DataSourceMapper extends BaseMapper<DataSource> {

    /**
     * query datasource by type
     * @param userId userId
     * @param type type
     * @return datasource list
     */
    List<DataSource> queryDataSourceByType(@Param("userId") int userId, @Param("type") Integer type);

    /**
     * datasource page
     * @param page page
     * @param userId userId
     * @param name name
     * @return datasource IPage
     */
    IPage<DataSource> selectPaging(IPage<DataSource> page,
                                   @Param("userId") int userId,
                                   @Param("name") String name);

    /**
     * query datasource by name
     * @param name name
     * @return datasource list
     */
    List<DataSource> queryDataSourceByName(@Param("name") String name);


    /**
     * query authed datasource
     * @param userId userId
     * @return datasource list
     */
    List<DataSource> queryAuthedDatasource(@Param("userId") int userId);

    /**
     * query datasource except userId
     * @param userId userId
     * @return datasource list
     */
    List<DataSource> queryDatasourceExceptUserId(@Param("userId") int userId);

    /**
     * list all datasource by type
     * @param type datasource type
     * @return datasource list
     */
    List<DataSource> listAllDataSourceByType(@Param("type") Integer type);


    /**
     * list authorized UDF function
     *
     * @param userId userId
     * @param dataSourceIds data source id array
     * @param <T> T
     * @return UDF function list
     */
    <T> List<DataSource> listAuthorizedDataSource(@Param("userId") int userId,@Param("dataSourceIds")T[] dataSourceIds);


}

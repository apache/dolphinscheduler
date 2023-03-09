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

import org.apache.dolphinscheduler.dao.entity.Environment;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * environment mapper interface
 */
public interface EnvironmentMapper extends BaseMapper<Environment> {

    /**
     * query environment by name
     *
     * @param name name
     * @return environment
     */
    Environment queryByEnvironmentName(@Param("environmentName") String name);

    /**
     * query environment by code
     *
     * @param environmentCode environmentCode
     * @return environment
     */
    Environment queryByEnvironmentCode(@Param("environmentCode") Long environmentCode);

    /**
     * query all environment list
     * @return environment list
     */
    List<Environment> queryAllEnvironmentList();

    /**
     * environment page
     * @param page page
     * @param searchName searchName
     * @return environment IPage
     */
    IPage<Environment> queryEnvironmentListPaging(IPage<Environment> page, @Param("searchName") String searchName);

    /**
     * delete environment by code
     *
     * @param code code
     * @return int
     */
    int deleteByCode(@Param("code") Long code);

    /**
     * queryEnvironmentListPagingByIds
     * @param page
     * @param ids
     * @param searchVal
     * @return
     */
    IPage<Environment> queryEnvironmentListPagingByIds(Page<Environment> page, @Param("ids") List<Integer> ids,
                                                       @Param("searchName") String searchVal);
}

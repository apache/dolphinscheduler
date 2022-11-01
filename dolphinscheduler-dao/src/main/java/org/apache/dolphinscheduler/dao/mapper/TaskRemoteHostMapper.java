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

import org.apache.dolphinscheduler.dao.entity.TaskRemoteHost;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * TaskRemoteHost Mapper interfaces
 */
public interface TaskRemoteHostMapper extends BaseMapper<TaskRemoteHost> {

    /**
     * query task remote host by name
     *
     * @param name name
     * @return task remote host
     */
    TaskRemoteHost queryByTaskRemoteHostName(@Param("remoteHostName") String name);

    /**
     * query task remote host by code
     *
     * @param code remote host code
     * @return task remote host
     */
    TaskRemoteHost queryByTaskRemoteHostCode(@Param("remoteHostCode") Long code);

    /**
     * query all task remote host list
     * @return list of task remote host
     */
    List<TaskRemoteHost> queryAllTaskRemoteHostList();

    /**
     * task remote host page
     * @param page page
     * @param searchName searchName
     * @return remote host IPage
     */
    IPage<TaskRemoteHost> queryTaskRemoteHostListPaging(IPage<TaskRemoteHost> page,
                                                        @Param("searchName") String searchName);

    /**
     * task remote host page by id list
     * @param page page
     * @param ids id list
     * @param searchVal search value
     * @return remote host IPage
     */
    IPage<TaskRemoteHost> queryTaskRemoteHostListPagingByIds(Page<TaskRemoteHost> page, @Param("ids") List<Integer> ids,
                                                             @Param("searchName") String searchVal);

    /**
     * delete task remote host by code
     *
     * @param code code
     * @return int
     */
    int deleteByCode(@Param("code") Long code);

}

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

import org.apache.dolphinscheduler.dao.entity.TaskGroup;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * the Dao interfaces of task group
 *
 * @author yinrui
 * @since 2021-08-07
 */
public interface TaskGroupMapper extends BaseMapper<TaskGroup> {

    int robTaskGroupResource(@Param("id") int id,
                             @Param("currentUseSize") int currentUseSize,
                             @Param("queueId") int queueId,
                             @Param("queueStatus") int queueStatus);

    /**
     * update table of task group
     *
     * @param id primary key
     * @return affected rows
     */
    int releaseTaskGroupResource(@Param("id") int id, @Param("useSize") int useSize,
                                 @Param("queueId") int queueId, @Param("queueStatus") int queueStatus);

    /**
     * select task groups paging
     *
     * @param page   page
     * @param name   name
     * @param status status
     * @return result page
     */
    IPage<TaskGroup> queryTaskGroupPaging(IPage<TaskGroup> page, @Param("name") String name,
                                          @Param("status") Integer status);

    /**
     * query by task group name
     *
     * @param userId user id
     * @param name   name
     * @return task group
     */
    TaskGroup queryByName(@Param("userId") int userId, @Param("name") String name);

    /**
     * Select the groupSize > useSize Count
     */
    int selectAvailableCountById(@Param("groupId") int groupId);

    int selectCountByIdStatus(@Param("id") int id, @Param("status") int status);

    IPage<TaskGroup> queryTaskGroupPagingByProjectCode(Page<TaskGroup> page, @Param("projectCode") Long projectCode);

    /**
     * listAuthorizedResource
     *
     * @param userId
     * @return
     */
    List<TaskGroup> listAuthorizedResource(@Param("userId") int userId);

    List<TaskGroup> selectByProjectCode(@Param("projectCode") long projectCode);
}

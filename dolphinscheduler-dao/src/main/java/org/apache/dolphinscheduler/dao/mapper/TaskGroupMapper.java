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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;

import org.apache.ibatis.annotations.Param;

/**
 * the Dao interfaces of task group
 * @author yinrui
 * @since 2021-08-07
 */
public interface TaskGroupMapper extends BaseMapper<TaskGroup> {

    /**
     * update the used size of a task group if the value of useSize column equals the useSize parameter
     * @param id primary key
     * @param oldUseSize old used size
     * @param newUseSize new used size
     * @return affected rows
     */
    int compardAndUpdateUsedStatus(@Param("id") Integer id, @Param("oldUseSize") int oldUseSize,@Param("newUseSize") int newUseSize);

    /**
     * select task groups paging
     * @param page page
     * @param userId user id
     * @param name name
     * @param status status
     * @return result page
     */
    IPage<TaskGroup> queryTaskGroupPaging(IPage<TaskGroup> page, @Param("userId") int userId,
                                          @Param("name") String name, @Param("status") Integer status);

    /**
     * query by task group name
     * @param userId user id
     * @param name name
     * @return task group
     */
    TaskGroup queryByName(@Param("userId") Integer userId, @Param("name")  String name);

}


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
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * the Dao interfaces of task group queue
 * @author yinrui
 * @since 2021-08-07
 */
public interface TaskGroupQueueMapper extends BaseMapper<TaskGroupQueue> {

    /**
     * select task group queues by some conditions
     * @param page page
     * @param groupId group id
     * @return task group queue list
     */
    IPage<TaskGroupQueue> queryTaskGroupQueuePaging(IPage<TaskGroupQueue> page,
                                                    @Param("groupId") Integer groupId
                                                    );

    TaskGroupQueue queryByTaskId(@Param("taskId") Integer taskId);


    /**
     * query by status
     * @param status status
     * @return result
     */
    List<TaskGroupQueue> queryByStatus(@Param("status") Integer status);

    /**
     * delete by task id
     * @param taskId task id
     * @return affected rows
     */
    int deleteByTaskId(@Param("taskId") Integer taskId);

    /**
     * update status by task id
     * @param taskId task id
     * @param status status
     * @return
     */
    int updateStatusByTaskId(@Param("taskId") Integer taskId, @Param("status") Integer status);

}


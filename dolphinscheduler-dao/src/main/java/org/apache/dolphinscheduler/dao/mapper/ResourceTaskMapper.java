/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.ResourcesTask;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * resource task relation mapper interface
 */
public interface ResourceTaskMapper extends BaseMapper<ResourcesTask> {

    Integer existResourceByTaskIdNFullName(@Param("taskId") int task_id, @Param("fullName") String fullName);

    int deleteIds(@Param("resIds") Integer[] resIds);

    int updateResource(@Param("id") int id, @Param("fullName") String fullName);

    List<ResourcesTask> selectBatchFullNames(@Param("fullNameArr") String[] fullNameArr);

    List<ResourcesTask> selectSubfoldersFullNames(@Param("folderPath") String folderPath);
}

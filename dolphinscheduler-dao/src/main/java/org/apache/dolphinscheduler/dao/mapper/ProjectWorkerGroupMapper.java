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

import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface ProjectWorkerGroupMapper extends BaseMapper<ProjectWorkerGroup> {

    int deleteByProjectCode(@Param("projectCode") Long projectCode);

    Set<String> queryAssignedWorkerGroupNamesByProjectCode(@Param("projectCode") Long projectCode);

    int deleteByProjectCodeAndWorkerGroups(@Param("projectCode") Long projectCode,
                                           @Param("workerGroups") List<String> workerGroups);

    List<ProjectWorkerGroup> queryByProjectCode(@Param("projectCode") Long projectCode);
}

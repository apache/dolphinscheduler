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

import org.apache.dolphinscheduler.common.enums.ContextType;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceContext;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface TaskInstanceContextMapper extends BaseMapper<TaskInstanceContext> {

    List<TaskInstanceContext> queryListByTaskInstanceIdAndContextType(@Param("taskInstanceId") int taskInstanceId,
                                                                      @Param("contextType") ContextType contextType);

    int deleteByTaskInstanceIdAndContextType(@Param("taskInstanceId") int taskInstanceId,
                                             @Param("contextType") ContextType contextType);

    int updateTaskInstanceContextByTaskInstanceIdAndContextType(@Param("taskInstanceId") int taskInstanceId,
                                                                @Param("contextType") ContextType contextType,
                                                                @Param("context") String context);

    List<TaskInstanceContext> batchQueryByTaskInstanceIdsAndContextType(@Param("taskInstanceIds") List<Integer> taskInstanceIds,
                                                                        @Param("contextType") ContextType contextType);
}

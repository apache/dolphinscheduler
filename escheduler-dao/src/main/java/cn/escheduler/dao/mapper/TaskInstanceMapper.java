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
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.*;
import cn.escheduler.dao.entity.ExecuteStatusCount;
import cn.escheduler.dao.entity.TaskInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface TaskInstanceMapper extends BaseMapper<TaskInstance> {


    List<Integer> queryTaskByProcessIdAndState(@Param("processInstanceId") Integer processInstanceId,
                                               @Param("state") Integer state);


    TaskInstance queryById(@Param("taskInstanceId") int taskInstanceId);

    List<TaskInstance> findValidTaskListByProcessId(@Param("processInstanceId") Integer processInstanceId,
                                                    @Param("flag") Flag flag);

    List<TaskInstance> queryByHostAndStatus(@Param("host") String host,
                                            @Param("states") String stateArray);

    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") String stateArray,
                                       @Param("destStatus") ExecutionStatus destStatus);

    TaskInstance queryByInstanceIdAndName(@Param("processInstanceId") int processInstanceId,
                                          @Param("name") String name);

    Integer countTask(@Param("userId") int userId,
                      @Param("userType") UserType userType,
                      @Param("projectIds") String projectIds,
                      @Param("taskIds") String taskIds);

    List<ExecuteStatusCount> countTaskInstanceStateByUser(@Param("userId") int userId,
                                                          @Param("userType") UserType userType,
                                                          @Param("startTime") Date startTime,
                                                          @Param("endTime") Date endTime,
                                                          @Param("projectIds") String projectIds);

    IPage<TaskInstance> queryTaskInstanceListPaging(IPage<TaskInstance> page,
                                                    @Param("projectId") int projectId,
                                                    @Param("processInstanceId") Integer processInstanceId,
                                                    @Param("searchVal") String searchVal,
                                                    @Param("taskName") String taskName,
                                                    @Param("states") String statusArray,
                                                    @Param("host") String host,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime
    );
}

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
import cn.escheduler.dao.entity.ProcessInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {

    ProcessInstance queryDetailById(@Param("processId") int processId);

    List<ProcessInstance> queryByHostAndStatus(@Param("host") String host, @Param("states") String stateArray);

    IPage<ProcessInstance> queryProcessInstanceListPaging(Page<ProcessInstance> page,
                                                          @Param("projectId") int projectId,
                                                          @Param("processDefinitionId") Integer processDefinitionId,
                                                          @Param("searchVal") String searchVal,
                                                          @Param("states") String statusArray,
                                                          @Param("host") String host,
                                                          @Param("startTime") Date startTime,
                                                          @Param("endTime") Date endTime
    );

    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") String stateArray);

    int updateProcessInstanceByState(@Param("originState") ExecutionStatus originState,
                                     @Param("destState") ExecutionStatus destState);


    ProcessInstance queryByTaskId(@Param("taskId") int taskId);


    List<ExecuteStatusCount> countInstanceStateByUser(
            @Param("userId") int userId,
            @Param("userType") UserType userType,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectIds") String projectIds);

    List<Integer> querySubIdListByParentId(@Param("parentInstanceId") int parentInstanceId);


    List<ProcessInstance> queryByProcessDefineId(@Param("processDefinitionId") int processDefinitionId,
                                                 @Param("size") int size);

    ProcessInstance queryByScheduleTime(@Param("processDefinitionId") int processDefinitionId,
                                        @Param("scheduleTime") String scheduleTime,
                                        @Param("excludeId") int excludeId,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);


    ProcessInstance queryLastSchedulerProcess(@Param("processDefinitionId") int definitionId,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    ProcessInstance queryLastRunningProcess(@Param("processDefinitionId") int definitionId,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("states") int[] stateArray);

    ProcessInstance queryLastManualProcess(@Param("processDefinitionId") int definitionId,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);
}

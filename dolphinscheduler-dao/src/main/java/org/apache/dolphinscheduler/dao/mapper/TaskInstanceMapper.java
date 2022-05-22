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
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * task instance mapper interface
 */
public interface TaskInstanceMapper extends BaseMapper<TaskInstance> {

    List<Integer> queryTaskByProcessIdAndState(@Param("processInstanceId") Integer processInstanceId,
                                               @Param("state") Integer state);

    List<TaskInstance> findValidTaskListByProcessId(@Param("processInstanceId") Integer processInstanceId,
                                                    @Param("flag") Flag flag);

    List<TaskInstance> queryByHostAndStatus(@Param("host") String host,
                                            @Param("states") int[] stateArray);

    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") int[] stateArray,
                                       @Param("destStatus") ExecutionStatus destStatus);

    TaskInstance queryByInstanceIdAndName(@Param("processInstanceId") int processInstanceId,
                                          @Param("name") String name);

    TaskInstance queryByInstanceIdAndCode(@Param("processInstanceId") int processInstanceId,
                                          @Param("taskCode") Long taskCode);

    Integer countTask(@Param("projectCodes") Long[] projectCodes,
                      @Param("taskIds") int[] taskIds);

    /**
     * Statistics task instance group by given project codes list by start time
     * <p>
     * We only need project codes to determine whether the task instance belongs to the user or not.
     *
     * @param startTime    Statistics start time
     * @param endTime      Statistics end time
     * @param projectCodes Project codes list to filter
     * @return List of ExecuteStatusCount
     */
    List<ExecuteStatusCount> countTaskInstanceStateByProjectCodes(@Param("startTime") Date startTime,
                                                                  @Param("endTime") Date endTime,
                                                                  @Param("projectCodes") Long[] projectCodes);

    /**
     * Statistics task instance group by given project codes list by submit time
     * <p>
     * We only need project codes to determine whether the task instance belongs to the user or not.
     *
     * @param startTime    Statistics start time
     * @param endTime      Statistics end time
     * @param projectCodes Project codes list to filter
     * @return List of ExecuteStatusCount
     */
    List<ExecuteStatusCount> countTaskInstanceStateByProjectCodesAndStatesBySubmitTime(@Param("startTime") Date startTime,
                                                                                       @Param("endTime") Date endTime,
                                                                                       @Param("projectCodes") Long[] projectCodes,
                                                                                       @Param("states") List<ExecutionStatus> states);

    IPage<TaskInstance> queryTaskInstanceListPaging(IPage<TaskInstance> page,
                                                    @Param("projectCode") Long projectCode,
                                                    @Param("processInstanceId") Integer processInstanceId,
                                                    @Param("processInstanceName") String processInstanceName,
                                                    @Param("searchVal") String searchVal,
                                                    @Param("taskName") String taskName,
                                                    @Param("executorId") int executorId,
                                                    @Param("states") int[] statusArray,
                                                    @Param("host") String host,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime
    );

    List<TaskInstance> loadAllInfosNoRelease(@Param("processInstanceId") int processInstanceId, @Param("status") int status);
}

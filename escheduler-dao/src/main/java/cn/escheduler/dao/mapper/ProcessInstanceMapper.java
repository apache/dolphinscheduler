package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.*;
import cn.escheduler.dao.entity.ExecuteStatusCount;
import cn.escheduler.dao.entity.ProcessInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {

    ProcessInstance queryDetailById(@Param("processId") int processId);

    List<ProcessInstance> queryByHostAndStatus(@Param("host") String host, @Param("states") String stateArray);

    List<ProcessInstance> queryProcessInstanceListPaging(@Param("projectId") int projectId,
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

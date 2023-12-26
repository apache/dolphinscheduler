package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.dto.trigger.TriggerCreateRequest;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Trigger;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

public interface TriggerService {

    Trigger createTrigger(User loginUser, TriggerCreateRequest triggerCreateRequest);

    Trigger updateSchedulesV2(User loginUser,
                               Integer triggerId,
                               ScheduleUpdateRequest scheduleUpdateRequest);

    Trigger getSchedule(User loginUser,
                         Integer triggerId);

    Result queryTrigger(User loginUser, long projectCode, long processDefineCode, String searchVal,
                        Integer pageNo, Integer pageSize);

    void deleteTriggerById(User loginUser, Integer triggerId);

    void onlineScheduler(User loginUser, Long projectCode, Integer schedulerId);

    void offlineScheduler(User loginUser, Long projectCode, Integer schedulerId);

    void offlineSchedulerByWorkflowCode(Long workflowDefinitionCode);
}

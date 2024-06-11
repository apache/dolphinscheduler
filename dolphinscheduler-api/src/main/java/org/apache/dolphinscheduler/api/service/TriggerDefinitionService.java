package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * trigger definition service
 */
public interface TriggerDefinitionService {

    Map<String, Object> createTriggerDefinition(User loginUser,
                                               long projectCode,
                                               String triggerDefinitionJson);

    Result queryTriggerDefinitionListPaging(User loginUser,
                                                   long projectCode,
                                                   String searchTriggerName,
                                                   String triggerType,
                                                   Integer pageNo,
                                                   Integer pageSize);
}

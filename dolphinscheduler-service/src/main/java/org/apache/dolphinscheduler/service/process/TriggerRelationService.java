package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.common.enums.TriggerType;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;
import org.springframework.stereotype.Component;

/**
 *  Trigger relation operator to db，because operator command process instance
 */
@Component
public interface TriggerRelationService {


    void saveTriggerTdoDb(TriggerType type, Long triggerCode, Integer jobId);

    TriggerRelation queryByTypeAndJobId(TriggerType triggerType, int jobId);

    int saveCommandTrigger(Integer commandId ,Integer processInstanceId);

    int saveProcessInstanceTrigger(Integer commandId ,Integer processInstanceId);
}

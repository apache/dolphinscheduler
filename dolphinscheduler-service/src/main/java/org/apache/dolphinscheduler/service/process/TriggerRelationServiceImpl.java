package org.apache.dolphinscheduler.service.process;

import java.util.Date;
import org.apache.dolphinscheduler.common.enums.TriggerType;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;
import org.apache.dolphinscheduler.dao.mapper.TriggerRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  Trigger relation operator to db
 */
@Component
public class TriggerRelationServiceImpl implements TriggerRelationService {
    @Autowired
    private TriggerRelationMapper triggerRelationMapper;

    public void saveTriggerTdoDb(TriggerType type, Long triggerCode, Integer jobId) {
        TriggerRelation triggerRelation = new TriggerRelation();
        triggerRelation.setTriggerType(type.getCode());
        triggerRelation.setJobId(jobId);
        triggerRelation.setTriggerCode(triggerCode);
        triggerRelation.setCreateTime(new Date());
        triggerRelation.setUpdateTime(new Date());
        triggerRelationMapper.upsert(triggerRelation);
    }

    public TriggerRelation queryByTypeAndJobId(TriggerType triggerType, int jobId) {
        return triggerRelationMapper.queryByTypeAndJobId(triggerType.getCode(), jobId);
    }


    @Override
    public int saveCommandTrigger(Integer commandId ,Integer processInstanceId) {
        TriggerRelation exist = queryByTypeAndJobId(TriggerType.PROCESS,processInstanceId);
        if(exist == null) {
            return 0;
        }
        saveTriggerTdoDb(TriggerType.COMMAND,exist.getTriggerCode(),commandId);
        return 1;
    }

    @Override
    public int  saveProcessInstanceTrigger(Integer commandId ,Integer processInstanceId) {
        TriggerRelation exist = queryByTypeAndJobId(TriggerType.COMMAND,commandId);
        if(exist == null) {
            return 0;
        }
        saveTriggerTdoDb(TriggerType.PROCESS,exist.getTriggerCode(),processInstanceId);
        return 1;
    }

}

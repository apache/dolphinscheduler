package org.apache.dolphinscheduler.service.process;

import java.util.Date;
import org.apache.dolphinscheduler.common.enums.TriggerType;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;
import org.apache.dolphinscheduler.dao.mapper.TriggerRelationMapper;
import org.apache.dolphinscheduler.service.cron.CronUtilsTest;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger Relation Service Test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TriggerRelationServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

  @InjectMocks
  private TriggerRelationServiceImpl triggerRelationService;
  @Mock
  private TriggerRelationMapper triggerRelationMapper;

  @Test
  public void saveTriggerTdoDb() {
    Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
    triggerRelationService.saveTriggerTdoDb(TriggerType.COMMAND, 1234567890L, 100);
  }

  @Test
  public void queryByTypeAndJobId() {
    Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
    Mockito.when(triggerRelationMapper.queryByTypeAndJobId(TriggerType.PROCESS.getCode(), 100))
        .thenReturn(getTriggerTdoDb());

    TriggerRelation triggerRelation1 = triggerRelationService.queryByTypeAndJobId(
        TriggerType.PROCESS, 100);
    Assertions.assertNotNull(triggerRelation1);
    TriggerRelation triggerRelation2 = triggerRelationService.queryByTypeAndJobId(
        TriggerType.PROCESS, 200);
    Assertions.assertNull(triggerRelation2);
  }


  @Test
  public void saveCommandTrigger() {
    Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
    Mockito.when(triggerRelationMapper.queryByTypeAndJobId(TriggerType.PROCESS.getCode(), 100))
        .thenReturn(getTriggerTdoDb());
    int result = -1;
    result = triggerRelationService.saveCommandTrigger(1234567890, 100);
    Assertions.assertTrue(result > 0);
    result = triggerRelationService.saveCommandTrigger(1234567890, 200);
    Assertions.assertTrue(result == 0);

  }

  @Test
  public void saveProcessInstanceTrigger() {
    Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
    Mockito.when(triggerRelationMapper.queryByTypeAndJobId(TriggerType.COMMAND.getCode(), 100))
        .thenReturn(getTriggerTdoDb());
    int result = -1;
    result = triggerRelationService.saveProcessInstanceTrigger(100, 1234567890);
    Assertions.assertTrue(result > 0);
    result = triggerRelationService.saveProcessInstanceTrigger(200, 1234567890);
    Assertions.assertTrue(result == 0);
  }


  private TriggerRelation getTriggerTdoDb() {
    TriggerRelation triggerRelation = new TriggerRelation();
    triggerRelation.setTriggerType(TriggerType.PROCESS.getCode());
    triggerRelation.setJobId(100);
    triggerRelation.setTriggerCode(1234567890L);
    triggerRelation.setCreateTime(new Date());
    triggerRelation.setUpdateTime(new Date());
    return triggerRelation;
  }
}
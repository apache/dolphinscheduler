package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TriggerOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class TriggerOffsetMapperTest extends BaseDaoTest {
    @Autowired
    private TriggerOffsetMapper triggerOffsetMapper;

    public TriggerOffset insertOne() {
        return insertOne(99);
    }

    public TriggerOffset insertOne(int userId) {
        TriggerOffset triggerOffset = new TriggerOffset();
        triggerOffset.setId(1);
        triggerOffset.setCode(888888L);
        return triggerOffset;
    }

    @Test
    public void testInsert() {
        TriggerOffset triggerDefinition = insertOne();
        Assertions.assertNotEquals(0, triggerDefinition.getId().intValue());
    }
}

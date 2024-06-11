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

import java.util.Date;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TriggerDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TriggerDefinitionMapperTest extends BaseDaoTest {
    @Autowired
    private TriggerDefinitionMapper triggerDefinitionMapper;

    public TriggerDefinition insertOne() {
        return insertOne(99);
    }

    public TriggerDefinition insertOne(int userId) {
        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setCode(888888L);
        triggerDefinition.setName("unit-test");
        triggerDefinition.setProjectCode(1L);
        triggerDefinition.setUserId(userId);
        triggerDefinition.setCreateTime(new Date());
        triggerDefinition.setUpdateTime(new Date());
        triggerDefinition.setTriggerType("unit-test");
        triggerDefinitionMapper.insert(triggerDefinition);
        return triggerDefinition;
    }

    @Test
    public void testInsert() {
        TriggerDefinition triggerDefinition = insertOne();
        Assertions.assertNotEquals(0, triggerDefinition.getId().intValue());
    }
}

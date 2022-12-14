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

import org.apache.dolphinscheduler.common.enums.ApiTriggerType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * trigger mapper test
 */
public class TriggerRelationMapperTest extends BaseDaoTest {

    @Autowired
    TriggerRelationMapper triggerRelationMapper;

    /**
     * test insert
     *
     * @return
     */
    @Test
    public void testInsert() {
        TriggerRelation expectedObj = createTriggerRelation();
        Assertions.assertTrue(expectedObj.getId() > 0);
    }

    /**
     * test select by id
     *
     * @return
     */
    @Test
    public void testSelectById() {
        TriggerRelation expectRelation = createTriggerRelation();
        TriggerRelation actualRelation = triggerRelationMapper.selectById(expectRelation.getId());
        Assertions.assertEquals(expectRelation, actualRelation);
    }

    /**
     * test select by type and job id
     *
     * @return
     */
    @Test
    public void testQueryByTypeAndJobId() {
        TriggerRelation expectRelation = createTriggerRelation();
        TriggerRelation actualRelation = triggerRelationMapper.queryByTypeAndJobId(
                expectRelation.getTriggerType(), expectRelation.getJobId());
        Assertions.assertEquals(expectRelation, actualRelation);
    }

    /**
     * test select by trigger code
     *
     * @return
     */
    @Test
    public void testQueryByTriggerRelationCode() {
        TriggerRelation expectRelation = createTriggerRelation();
        List<TriggerRelation> actualRelations = triggerRelationMapper.queryByTriggerRelationCode(
                expectRelation.getTriggerCode());
        Assertions.assertEquals(actualRelations.size(), 1);
    }

    /**
     * test select by type and trigger code
     *
     * @return
     */
    @Test
    public void testQueryByTriggerRelationCodeAndType() {
        TriggerRelation expectRelation = createTriggerRelation();
        List<TriggerRelation> actualRelations = triggerRelationMapper.queryByTriggerRelationCodeAndType(
                expectRelation.getTriggerCode(), expectRelation.getTriggerType());
        Assertions.assertEquals(actualRelations.size(), 1);
    }

    @Test
    public void testUpsert() {
        TriggerRelation expectRelation = createTriggerRelation();
        triggerRelationMapper.upsert(expectRelation);
        TriggerRelation actualRelation = triggerRelationMapper.selectById(expectRelation.getId());
        Assertions.assertEquals(expectRelation, actualRelation);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        TriggerRelation expectRelation = createTriggerRelation();
        triggerRelationMapper.deleteById(expectRelation.getId());
        TriggerRelation actualRelation = triggerRelationMapper.selectById(expectRelation.getId());
        Assertions.assertNull(actualRelation);
    }

    /**
     * create TriggerRelation and insert
     *
     * @return TriggerRelation
     * @throws Exception
     */
    private TriggerRelation createTriggerRelation() {
        TriggerRelation triggerRelation = new TriggerRelation();
        triggerRelation.setTriggerCode(4567890);
        triggerRelation.setTriggerType(ApiTriggerType.COMMAND.getCode());
        triggerRelation.setJobId(99);
        triggerRelation.setCreateTime(DateUtils.getCurrentDate());
        triggerRelation.setUpdateTime(DateUtils.getCurrentDate());

        triggerRelationMapper.insert(triggerRelation);
        return triggerRelation;
    }

}

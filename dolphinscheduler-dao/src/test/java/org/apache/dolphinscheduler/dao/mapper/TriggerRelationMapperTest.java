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

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.apache.dolphinscheduler.common.enums.TriggerType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * trigger mapper test
 */
public class TriggerRelationMapperTest extends BaseDaoTest {

    @Autowired
    private TriggerRelationMapper triggerRelationMapper;

    /**
     * test insert
     *
     * @return
     */
    @Test
    public void testInsert() {
        TriggerRelation expectedObj = createTriggerRelation();
        assertThat(expectedObj.getId(), greaterThan(0));
    }

    /**
     * test select by id
     *
     * @return
     */
    @Test
    public void testSelectById() {
        TriggerRelation expectedAlert = createTriggerRelation();
        TriggerRelation actualAlert = triggerRelationMapper.selectById(expectedAlert.getId());
        assertEquals(expectedAlert, actualAlert);
    }


    /**
     * test delete
     */
    @Test
    public void testDelete() {
        TriggerRelation expectedAlert = createTriggerRelation();

        triggerRelationMapper.deleteById(expectedAlert.getId());

        TriggerRelation actualAlert = triggerRelationMapper.selectById(expectedAlert.getId());

        assertNull(actualAlert);
    }


    /**
     * create alert
     *
     * @return alert
     * @throws Exception
     */
    private TriggerRelation createTriggerRelation() {
        TriggerRelation triggerRelation = new TriggerRelation();
        triggerRelation.setTriggerCode(4567890);
        triggerRelation.setTriggerType(TriggerType.COMMAND.getCode());
        triggerRelation.setJobId(99);
        triggerRelation.setCreateTime(DateUtils.getCurrentDate());
        triggerRelation.setUpdateTime(DateUtils.getCurrentDate());

        triggerRelationMapper.insert(triggerRelation);
        return triggerRelation;
    }

}

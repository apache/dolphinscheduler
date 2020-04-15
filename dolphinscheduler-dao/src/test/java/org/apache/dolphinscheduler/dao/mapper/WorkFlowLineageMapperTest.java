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

import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class WorkFlowLineageMapperTest {
    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Test
    public void testQueryByName() {
        List<WorkFlowLineage> workFlowLineages = workFlowLineageMapper.queryByName("test",1);
        Assert.assertNotEquals(workFlowLineages.size(), 0);
    }


    @Test
    public void testQueryByIds() {
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        List<WorkFlowLineage> workFlowLineages = workFlowLineageMapper.queryByIds(ids,1);
        Assert.assertNotEquals(workFlowLineages.size(), 0);
    }

    @Test
    public void testQuerySourceTarget() {
        List<WorkFlowRelation> workFlowRelations = workFlowLineageMapper.querySourceTarget(1);
        Assert.assertNotEquals(workFlowRelations.size(), 0);
    }
}

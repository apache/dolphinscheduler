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

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessTaskRelationMapperTest extends BaseDaoTest {

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private ProcessTaskRelation insertOne() {
        //insertOne
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setName("def 1");

        processTaskRelation.setProjectCode(1L);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPostTaskCode(3L);
        processTaskRelation.setPreTaskCode(2L);
        processTaskRelation.setUpdateTime(new Date());
        processTaskRelation.setCreateTime(new Date());
        processTaskRelationMapper.insert(processTaskRelation);
        return processTaskRelation;
    }

    @Test
    public void testQueryByProcessCode() {
        ProcessTaskRelation processTaskRelation = insertOne();
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByProcessCode(1L, 1L);
        Assertions.assertNotEquals(processTaskRelations.size(), 0);
    }

    @Test
    public void testQueryByTaskCode() {
        ProcessTaskRelation processTaskRelation = insertOne();
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByTaskCode(2L);
        Assertions.assertNotEquals(processTaskRelations.size(), 0);
    }

    @Test
    public void testQueryByTaskCodes() {
        ProcessTaskRelation processTaskRelation = insertOne();

        Long[] codes = Arrays.array(1L, 2L);
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationMapper.queryByTaskCodes(codes);
        Assertions.assertNotEquals(processTaskRelations.size(), 0);
    }

    @Test
    public void testDeleteByCode() {
        ProcessTaskRelation processTaskRelation = insertOne();
        int i = processTaskRelationMapper.deleteByCode(1L, 1L);
        Assertions.assertNotEquals(i, 0);
    }

}

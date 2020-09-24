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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.apache.dolphinscheduler.dao.entity.ProcessTag;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProcessTagMapperTest {

    @Autowired
    ProcessTagMapper processTagMapper;

    /**
     * insert
     * @return ProcessTag
     */
    private ProcessTag inserOne() {
        //insertone
        ProcessTag processTag = new ProcessTag();
        processTag.setProcessID(1001);
        processTag.settagID(110);
        processTagMapper.insert(processTag);
        return processTag;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        //insertOne
        ProcessTag processTag = inserOne();
        int update = processTagMapper.updateById(processTag);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        ProcessTag processTag = inserOne();
        int delete = processTagMapper.deleteById(processTag.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProcessTag processTag = inserOne();
        //query
        List<ProcessTag> processTags = processTagMapper.selectList(null);
        Assert.assertNotEquals(processTags.size(), 0);
    }

    @Test
    public void deleteProcessRelation() {
        ProcessTag processTag = inserOne();
        int delete = processTagMapper.deleteProcessRelation(processTag.getProcessID(),processTag.gettagID());
        assertThat(delete,greaterThanOrEqualTo(1));
    }
}
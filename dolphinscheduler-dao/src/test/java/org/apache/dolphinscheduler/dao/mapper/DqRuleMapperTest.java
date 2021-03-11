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

import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.dao.entity.DqRule;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * DqRuleMapperTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class DqRuleMapperTest {

    @Autowired
    private DqRuleMapper dqRuleMapper;

    @Test
    public void testQueryRuleListPaging() {
    //        ProcessDefinition processDefinition = insertOne();
    //        Page<ProcessDefinition> page = new Page(1, 3);
    //        IPage<ProcessDefinition> processDefinitionIPage = dqRuleMapper.queryRuleListPaging(page, "def", 101, 1010, true);
    //        Assert.assertNotEquals(processDefinitionIPage.getTotal(), 0);
    }

    private DqRule insertOne() {
        DqRule dqRule = new DqRule();
        dqRule.setId(1);
        dqRule.setName("规则1");
        dqRule.setType(RuleType.SINGLE_TABLE);
        dqRule.setUserId(1);
        dqRule.setUserName("admin");
        dqRule.setCreateTime(new Date());
        dqRule.setUpdateTime(new Date());

        dqRuleMapper.insert(dqRule);
        return dqRule;
    }
}

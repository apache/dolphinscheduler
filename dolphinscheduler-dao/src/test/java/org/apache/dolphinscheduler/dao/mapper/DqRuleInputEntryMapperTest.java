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
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * DQC rule mapper UT.
 */
public class DqRuleInputEntryMapperTest extends BaseDaoTest {

    @Autowired
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Autowired
    private DqRuleMapper dqRuleMapper;

    @Test
    public void testDqcRulePageList() {

        Page<DqRule> page = new Page<>(1, 10);

        IPage<DqRule> dqRulePage =
                dqRuleMapper.queryRuleListPaging(
                        page,
                        "",
                        -1,
                        null,
                        null);

        dqRulePage.getRecords().forEach(rule -> {
            final List<DqRuleInputEntry> ruleInputEntryList = dqRuleInputEntryMapper.getRuleInputEntryList(1);
            assert ruleInputEntryList != null;
        });
    }
}

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

import org.apache.dolphinscheduler.common.enums.dq.ExecuteSqlType;
import org.apache.dolphinscheduler.common.enums.dq.FormType;
import org.apache.dolphinscheduler.common.enums.dq.InputType;
import org.apache.dolphinscheduler.common.enums.dq.OptionSourceType;
import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.common.enums.dq.ValueType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private List<DqRule> getRuleList() {
        List<DqRule> list = new ArrayList<>();
        DqRule rule = new DqRule();
        rule.setId(1);
        rule.setName("空值检测");
        rule.setType(RuleType.SINGLE_TABLE);
        rule.setUserId(1);
        rule.setUserName("admin");
        rule.setCreateTime(new Date());
        rule.setUpdateTime(new Date());

        list.add(rule);

        return list;
    }

    private List<DqRuleInputEntry> getRuleInputEntryList() {
        List<DqRuleInputEntry> list = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT);
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue("JDBC");
        srcConnectorType.setPlaceholder("Please select the source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DEFAULT);
        srcConnectorType.setOptions("[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]");
        srcConnectorType.setInputType(InputType.DEFAULT);
        srcConnectorType.setValueType(ValueType.NUMBER);
        srcConnectorType.setEmit(true);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT);
        statisticsName.setCanEdit(true);
        statisticsName.setShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsName.setInputType(InputType.DEFAULT);
        statisticsName.setValueType(ValueType.STRING);
        statisticsName.setEmit(false);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA);
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL);
        statisticsExecuteSql.setEmit(false);

        list.add(srcConnectorType);
        list.add(statisticsName);
        list.add(statisticsExecuteSql);

        return list;
    }

    private List<DqRuleExecuteSql> getRuleExecuteSqlList() {
        List<DqRuleExecuteSql> list = new ArrayList<>();

        DqRuleExecuteSql executeSqlDefinition = new DqRuleExecuteSql();
        executeSqlDefinition.setIndex(0);
        executeSqlDefinition.setSql("SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})");
        executeSqlDefinition.setTableAlias("total_count");
        executeSqlDefinition.setExecuteSqlType(ExecuteSqlType.COMPARISON);
        list.add(executeSqlDefinition);

        return list;
    }
}

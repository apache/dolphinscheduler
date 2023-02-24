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

package org.apache.dolphinscheduler.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DqRuleServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.RuleType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.params.base.FormType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = ApiApplicationServer.class)
public class DqRuleServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @InjectMocks
    private DqRuleServiceImpl dqRuleService;

    @Mock
    DqRuleMapper dqRuleMapper;

    @Mock
    DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Mock
    DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Mock
    DataSourceMapper dataSourceMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    public void testGetRuleFormCreateJsonById() {
        String json = "[{\"field\":\"src_connector_type\",\"name\":\"源数据类型\",\"props\":{\"placeholder\":"
                + "\"Please select the source connector type\",\"size\":\"small\"},\"type\":\"select\",\"title\":"
                + "\"源数据类型\",\"value\":\"JDBC\",\"emit\":[\"change\"],\"options\":[{\"label\":\"HIVE\",\"value\":"
                + "\"HIVE\",\"disabled\":false},{\"label\":\"JDBC\",\"value\":\"JDBC\",\"disabled\":false}]},{\"props\":"
                + "{\"disabled\":false,\"rows\":2,\"placeholder\":\"Please enter statistics name, the alias in "
                + "statistics execute sql\",\"size\":\"small\"},\"field\":\"statistics_name\",\"name\":"
                + "\"统计值名\",\"type\":\"input\",\"title\":\"统计值名\",\"validate\":[{\"required\":true,\"type\":"
                + "\"string\",\"trigger\":\"blur\"}]},{\"props\":{\"disabled\":false,\"type\":\"textarea\",\"rows\":"
                + "1,\"placeholder\":\"Please enter the statistics execute sql\",\"size\":\"small\"},\"field\":"
                + "\"statistics_execute_sql\",\"name\":\"统计值计算SQL\",\"type\":\"input\",\"title\":"
                + "\"统计值计算SQL\",\"validate\":[{\"required\":true,\"type\":\"string\",\"trigger\":\"blur\"}]}]";
        when(dqRuleInputEntryMapper.getRuleInputEntryList(1)).thenReturn(getRuleInputEntryList());
        Map<String, Object> result = dqRuleService.getRuleFormCreateJsonById(1);
        Assertions.assertEquals(json, result.get(Constants.DATA_LIST));
    }

    @Test
    public void testQueryAllRuleList() {
        when(dqRuleMapper.selectList(new QueryWrapper<>())).thenReturn(getRuleList());
        Map<String, Object> result = dqRuleService.queryAllRuleList();
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGetDatasourceOptionsById() {
        when(dataSourceMapper.listAllDataSourceByType(DbType.MYSQL.getCode())).thenReturn(dataSourceList());
        Map<String, Object> result = dqRuleService.queryAllRuleList();
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryRuleListPaging() {

        String searchVal = "";
        int ruleType = 0;
        Date start = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-01-02 00:00:00");

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.DATA_QUALITY,
                loginUser.getId(), null, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.DATA_QUALITY, null, 0,
                baseServiceLogger)).thenReturn(true);
        Page<DqRule> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(getRuleList());

        when(dqRuleMapper.queryRuleListPaging(
                any(IPage.class), eq(""), eq(ruleType), eq(start), eq(end))).thenReturn(page);

        when(dqRuleInputEntryMapper.getRuleInputEntryList(1)).thenReturn(getRuleInputEntryList());
        when(dqRuleExecuteSqlMapper.getExecuteSqlList(1)).thenReturn(getRuleExecuteSqlList());

        Result result = dqRuleService.queryRuleListPaging(
                loginUser, searchVal, 0, "2020-01-01 00:00:00", "2020-01-02 00:00:00", 1, 10);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), result.getCode());
    }

    private List<DataSource> dataSourceList() {
        List<DataSource> dataSourceList = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setName("dolphinscheduler");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(1);
        dataSource.setUserName("admin");
        dataSource.setConnectionParams("");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());
        dataSourceList.add(dataSource);

        return dataSourceList;
    }

    private List<DqRule> getRuleList() {
        List<DqRule> list = new ArrayList<>();
        DqRule rule = new DqRule();
        rule.setId(1);
        rule.setName("空值检测");
        rule.setType(RuleType.SINGLE_TABLE.getCode());
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
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setValue("JDBC");
        srcConnectorType.setPlaceholder("Please select the source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcConnectorType
                .setOptions("[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]");
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setIsEmit(true);
        srcConnectorType.setIsValidate(true);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(true);
        statisticsName.setIsShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());
        statisticsName.setIsEmit(false);
        statisticsName.setIsValidate(true);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA.getFormType());
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setIsShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL.getCode());
        statisticsExecuteSql.setIsEmit(false);
        statisticsExecuteSql.setIsValidate(true);

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
        executeSqlDefinition.setType(ExecuteSqlType.COMPARISON.getCode());
        list.add(executeSqlDefinition);

        return list;
    }
}

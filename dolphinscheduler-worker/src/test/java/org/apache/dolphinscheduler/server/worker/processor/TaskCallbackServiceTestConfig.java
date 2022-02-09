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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.datasource.SpringConnectionFactory;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DqComparisonTypeMapper;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.mapper.DqTaskStatisticsValueMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dependency config
 */
@Configuration
public class TaskCallbackServiceTestConfig {

    @Bean
    public AlertDao alertDao() {
        return new AlertDao();
    }

    @Bean
    public AlertMapper alertMapper() {
        return Mockito.mock(AlertMapper.class);
    }

    @Bean
    public ProcessService processService() {
        return Mockito.mock(ProcessService.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mockito.mock(UserMapper.class);
    }

    @Bean
    public ProcessDefinitionMapper processDefineMapper() {
        return Mockito.mock(ProcessDefinitionMapper.class);
    }

    @Bean
    public ProcessInstanceMapper processInstanceMapper() {
        return Mockito.mock(ProcessInstanceMapper.class);
    }

    @Bean
    public DataSourceMapper dataSourceMapper() {
        return Mockito.mock(DataSourceMapper.class);
    }

    @Bean
    public ProcessInstanceMapMapper processInstanceMapMapper() {
        return Mockito.mock(ProcessInstanceMapMapper.class);
    }

    @Bean
    public TaskInstanceMapper taskInstanceMapper() {
        return Mockito.mock(TaskInstanceMapper.class);
    }

    @Bean
    public CommandMapper commandMapper() {
        return Mockito.mock(CommandMapper.class);
    }

    @Bean
    public ScheduleMapper scheduleMapper() {
        return Mockito.mock(ScheduleMapper.class);
    }

    @Bean
    public UdfFuncMapper udfFuncMapper() {
        return Mockito.mock(UdfFuncMapper.class);
    }

    @Bean
    public ResourceMapper resourceMapper() {
        return Mockito.mock(ResourceMapper.class);
    }

    @Bean
    public ResourceUserMapper resourceUserMapper() {
        return Mockito.mock(ResourceUserMapper.class);
    }

    @Bean
    public ErrorCommandMapper errorCommandMapper() {
        return Mockito.mock(ErrorCommandMapper.class);
    }

    @Bean
    public TenantMapper tenantMapper() {
        return Mockito.mock(TenantMapper.class);
    }

    @Bean
    public ProjectMapper projectMapper() {
        return Mockito.mock(ProjectMapper.class);
    }

    @Bean
    public AlertPluginInstanceMapper alertPluginInstanceMapper() {
        return Mockito.mock(AlertPluginInstanceMapper.class);
    }

    @Bean
    public AlertGroupMapper alertGroupMapper() {
        return Mockito.mock(AlertGroupMapper.class);
    }

    @Bean
    public PluginDefineMapper pluginDefineMapper() {
        return Mockito.mock(PluginDefineMapper.class);
    }

    @Bean
    public DqExecuteResultMapper dqExecuteResultMapper() {
        return Mockito.mock(DqExecuteResultMapper.class);
    }

    @Bean
    public DqRuleMapper dqRuleMapper() {
        return Mockito.mock(DqRuleMapper.class);
    }

    @Bean
    public DqRuleInputEntryMapper dqRuleInputEntryMapper() {
        return Mockito.mock(DqRuleInputEntryMapper.class);
    }

    @Bean
    public DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper() {
        return Mockito.mock(DqRuleExecuteSqlMapper.class);
    }

    @Bean
    public DqComparisonTypeMapper dqComparisonTypeMapper() {
        return Mockito.mock(DqComparisonTypeMapper.class);
    }

    @Bean
    public DqTaskStatisticsValueMapper dqTaskStatisticsValueMapper() {
        return Mockito.mock(DqTaskStatisticsValueMapper.class);
    }

    @Bean
    public SpringConnectionFactory springConnectionFactory() {
        return Mockito.mock(SpringConnectionFactory.class);
    }

}

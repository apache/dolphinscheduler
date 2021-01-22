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

package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.server.master.cache.impl.TaskInstanceCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.host.HostManager;
import org.apache.dolphinscheduler.server.master.dispatch.host.RandomHostManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dependency config
 */
@Configuration
public class DependencyConfig {

    @Bean
    public AlertDao alertDao() {
        return new AlertDao();
    }

    @Bean
    public AlertMapper alertMapper() {
        return Mockito.mock(AlertMapper.class);
    }

    @Bean
    public UserAlertGroupMapper userAlertGroupMapper() {
        return Mockito.mock(UserAlertGroupMapper.class);
    }

    @Bean
    public TaskInstanceCacheManagerImpl taskInstanceCacheManagerImpl(){
        return Mockito.mock(TaskInstanceCacheManagerImpl.class);
    }

    @Bean
    public ProcessService processService(){
        return Mockito.mock(ProcessService.class);
    }

    @Bean
    public MasterConfig masterConfig(){
        return Mockito.mock(MasterConfig.class);
    }
    @Bean
    public UserMapper userMapper(){
        return Mockito.mock(UserMapper.class);
    }

    @Bean
    public ProcessDefinitionMapper processDefineMapper(){
        return Mockito.mock(ProcessDefinitionMapper.class);
    }

    @Bean
    public ProcessInstanceMapper processInstanceMapper(){
        return Mockito.mock(ProcessInstanceMapper.class);
    }

    @Bean
    public DataSourceMapper dataSourceMapper(){
        return Mockito.mock(DataSourceMapper.class);
    }

    @Bean
    public ProcessInstanceMapMapper processInstanceMapMapper(){
        return Mockito.mock(ProcessInstanceMapMapper.class);
    }

    @Bean
    public TaskInstanceMapper taskInstanceMapper(){
        return Mockito.mock(TaskInstanceMapper.class);
    }

    @Bean
    public CommandMapper commandMapper(){
        return Mockito.mock(CommandMapper.class);
    }

    @Bean
    public ScheduleMapper scheduleMapper(){
        return Mockito.mock(ScheduleMapper.class);
    }

    @Bean
    public UdfFuncMapper udfFuncMapper(){
        return Mockito.mock(UdfFuncMapper.class);
    }

    @Bean
    public ResourceMapper resourceMapper(){
        return Mockito.mock(ResourceMapper.class);
    }



    @Bean
    public ErrorCommandMapper errorCommandMapper(){
        return Mockito.mock(ErrorCommandMapper.class);
    }

    @Bean
    public TenantMapper tenantMapper(){
        return Mockito.mock(TenantMapper.class);
    }

    @Bean
    public ProjectMapper projectMapper(){
        return Mockito.mock(ProjectMapper.class);
    }

    @Bean
    public TaskCallbackService taskCallbackService(){
        return Mockito.mock(TaskCallbackService.class);
    }

    @Bean
    public HostManager hostManager(){
        return new RandomHostManager();
    }

    @Bean
    public TaskResponseService taskResponseService(){
        return Mockito.mock(TaskResponseService.class);
    }

    @Bean
    public TaskPriorityQueue taskPriorityQueue(){
        return new TaskPriorityQueueImpl();
    }
}

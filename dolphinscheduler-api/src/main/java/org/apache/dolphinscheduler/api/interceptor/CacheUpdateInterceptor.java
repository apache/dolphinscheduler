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

package org.apache.dolphinscheduler.api.interceptor;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.service.cache.CacheNotifyService;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * the interceptor for mybatis update operation
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class CacheUpdateInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(CacheUpdateInterceptor.class);

    private static final String ET = "et";

    @Autowired
    private CacheNotifyService cacheNotifyService;

    @PostConstruct
    private void init() {
        logger.info("cache update interceptor init...");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];

        Object result = invocation.proceed();

        if (SqlCommandType.UPDATE == sqlCommandType || SqlCommandType.DELETE == sqlCommandType) {
            if (parameter instanceof Map) {
                Map paramMap = (Map) parameter;
                if (paramMap.containsKey(ET)) {
                    cacheExpireNotify(paramMap.get(ET));
                }
            } else {
                cacheExpireNotify(parameter);
            }
        }

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }

    /**
     * when object update, notify expire cache
     */
    private void cacheExpireNotify(Object updateObj) {
        CacheType cacheType = null;
        if (updateObj instanceof Tenant) {
            cacheType = CacheType.TENANT;
        } else if (updateObj instanceof User) {
            cacheType = CacheType.USER;
        } else if (updateObj instanceof Queue) {
            cacheType = CacheType.QUEUE;
        } else if (updateObj instanceof ProcessDefinition) {
            cacheType = CacheType.PROCESS_DEFINITION;
        } else if (updateObj instanceof ProcessTaskRelation) {
            cacheType = CacheType.PROCESS_TASK_RELATION;
        } else if (updateObj instanceof TaskDefinition) {
            cacheType = CacheType.TASK_DEFINITION;
        }

        if (cacheType != null) {
            cacheNotifyService.notifyMaster(new CacheExpireCommand(cacheType, updateObj).convert2Command());
        }
    }
}

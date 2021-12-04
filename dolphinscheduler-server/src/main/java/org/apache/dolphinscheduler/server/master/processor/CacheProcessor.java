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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * cache process from master/api
 */
public class CacheProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(CacheProcessor.class);

    private CacheManager cacheManager;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.CACHE_EXPIRE == command.getType(), String.format("invalid command type: %s", command.getType()));

        CacheExpireCommand cacheExpireCommand = JSONUtils.parseObject(command.getBody(), CacheExpireCommand.class);

        logger.info("received command : {}", cacheExpireCommand);

        this.cacheExpire(cacheExpireCommand);
    }

    private void cacheExpire(CacheExpireCommand cacheExpireCommand) {
        if (cacheManager == null) {
            cacheManager = SpringApplicationContext.getBean(CacheManager.class);
        }

        Object object = JSONUtils.parseObject(cacheExpireCommand.getUpdateObjJson(), cacheExpireCommand.getUpdateObjClass());
        if (object == null) {
            return;
        }

        CacheType cacheType = cacheExpireCommand.getCacheType();
        switch (cacheType) {
            case TENANT:
                if (object instanceof Tenant) {
                    Tenant tenant = (Tenant) object;
                    tenantCacheExpire(tenant);
                }
                break;
            case USER:
                if (object instanceof User) {
                    User user = (User) object;
                    userCacheExpire(user);
                }
                break;
            case QUEUE:
                if (object instanceof Queue) {
                    Queue queue = (Queue) object;
                    queueCacheExpire(queue);
                }
                break;
            case PROCESS_DEFINITION:
                if (object instanceof ProcessDefinition) {
                    ProcessDefinition processDefinition = (ProcessDefinition) object;
                    processDefinitionCacheExpire(processDefinition);
                }
                break;
            case TASK_DEFINITION:
                if (object instanceof TaskDefinition) {
                    TaskDefinition taskDefinition = (TaskDefinition) object;
                    taskDefinitionCacheExpire(taskDefinition);
                }
                break;
            case PROCESS_TASK_RELATION:
                if (object instanceof ProcessTaskRelation) {
                    ProcessTaskRelation processTaskRelation = (ProcessTaskRelation) object;
                    processTaskRelationCacheExpire(processTaskRelation);
                }
                break;
            case WORKER_GROUP:
                if (object instanceof WorkerGroup) {
                    WorkerGroup workerGroup = (WorkerGroup) object;
                    workerGroupCacheExpire(workerGroup);
                }
                break;
            case SCHEDULE:
                if (object instanceof Schedule) {
                    Schedule schedule = (Schedule) object;
                    scheduleCacheExpire(schedule);
                }
                break;
            default:
                logger.error("no support cache type:{}", cacheType);
        }

        // if delete operation, just send key
        if (object instanceof String) {
            Cache cache = cacheManager.getCache(cacheType.getCacheName());
            if (cache != null) {
                cache.evict(object);
                logger.info("cache evict, type:{}, key:{}", cacheType.getCacheName(), object);
            }
        }
    }

    private void tenantCacheExpire(Tenant tenant) {
        Cache cache = cacheManager.getCache(CacheType.TENANT.getCacheName());
        if (cache != null) {
            cache.evict(tenant.getId());
            logger.info("cache evict, type:{}, key:{}", CacheType.TENANT.getCacheName(), tenant.getId());
        }
    }

    private void userCacheExpire(User user) {
        Cache cache = cacheManager.getCache(CacheType.USER.getCacheName());
        if (cache != null) {
            cache.evict(user.getId());
            logger.info("cache evict, type:{}, key:{}", CacheType.USER.getCacheName(), user.getId());
        }
    }

    private void queueCacheExpire(Queue queue) {
        Cache cache = cacheManager.getCache(CacheType.USER.getCacheName());
        if (cache != null) {
            cache.clear();
            logger.info("cache evict, type:{}, clear", CacheType.USER.getCacheName());
        }
    }

    private void processDefinitionCacheExpire(ProcessDefinition processDefinition) {
        Cache cache = cacheManager.getCache(CacheType.PROCESS_DEFINITION.getCacheName());
        if (cache != null) {
            cache.evict(processDefinition.getCode());
            cache.evict(processDefinition.getCode() + "_" + processDefinition.getVersion());
            logger.info("cache evict, type:{}, key:{}",
                    CacheType.PROCESS_DEFINITION.getCacheName(), processDefinition.getCode() + "_" + processDefinition.getVersion());
        }
    }

    private void processTaskRelationCacheExpire(ProcessTaskRelation processTaskRelation) {
        Cache cache = cacheManager.getCache(CacheType.PROCESS_TASK_RELATION.getCacheName());
        if (cache != null) {
            cache.evict(processTaskRelation.getProjectCode() + "_" + processTaskRelation.getProcessDefinitionCode());
            logger.info("cache evict, type:{}, key:{}",
                    CacheType.PROCESS_TASK_RELATION.getCacheName(), processTaskRelation.getProjectCode() + "_" + processTaskRelation.getProcessDefinitionCode());
        }
    }

    private void taskDefinitionCacheExpire(TaskDefinition taskDefinition) {
        Cache cache = cacheManager.getCache(CacheType.TASK_DEFINITION.getCacheName());
        if (cache != null) {
            cache.evict(taskDefinition.getCode() + "_" + taskDefinition.getVersion());
            logger.info("cache evict, type:{}, key:{}",
                    CacheType.TASK_DEFINITION.getCacheName(), taskDefinition.getCode() + "_" + taskDefinition.getVersion());
        }
    }

    private void workerGroupCacheExpire(WorkerGroup workerGroup) {
        Cache cache = cacheManager.getCache(CacheType.WORKER_GROUP.getCacheName());
        if (cache != null) {
            cache.evict("all");
            logger.info("cache evict, type:{}, key:{}",
                    CacheType.WORKER_GROUP.getCacheName(), "all");
        }
    }

    private void scheduleCacheExpire(Schedule schedule) {
        Cache cache = cacheManager.getCache(CacheType.SCHEDULE.getCacheName());
        if (cache != null) {
            cache.evict(schedule.getProcessDefinitionCode());
            logger.info("cache evict, type:{}, key:{}",
                    CacheType.SCHEDULE.getCacheName(), schedule.getProcessDefinitionCode());
        }
    }
}

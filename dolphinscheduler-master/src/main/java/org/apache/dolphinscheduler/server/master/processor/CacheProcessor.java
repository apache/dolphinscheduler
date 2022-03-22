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
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * cache process from master/api
 */
@Component
public class CacheProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(CacheProcessor.class);

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.CACHE_EXPIRE == command.getType(), String.format("invalid command type: %s", command.getType()));

        CacheExpireCommand cacheExpireCommand = JSONUtils.parseObject(command.getBody(), CacheExpireCommand.class);

        logger.info("received command : {}", cacheExpireCommand);

        this.cacheExpire(cacheExpireCommand);
    }

    private void cacheExpire(CacheExpireCommand cacheExpireCommand) {

        if (cacheExpireCommand.getCacheKey().isEmpty()) {
            return;
        }

        CacheType cacheType = cacheExpireCommand.getCacheType();
        Cache cache = cacheManager.getCache(cacheType.getCacheName());
        if (cache != null) {
            cache.evict(cacheExpireCommand.getCacheKey());
            logger.info("cache evict, type:{}, key:{}", cacheType.getCacheName(), cacheExpireCommand.getCacheKey());
        }
    }
}

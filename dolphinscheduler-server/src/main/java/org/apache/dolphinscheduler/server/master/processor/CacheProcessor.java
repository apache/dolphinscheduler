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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.impl.CacheProxyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * cache process from master/api
 */
public class CacheProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(CacheProcessor.class);

    private CacheProxyFactory cacheProxyFactory;

    public CacheProcessor() {
        this.cacheProxyFactory = SpringApplicationContext.getBean(CacheProxyFactory.class);
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.CACHE_EXPIRE == command.getType(), String.format("invalid command type: %s", command.getType()));

        CacheExpireCommand cacheExpireCommand = JSONUtils.parseObject(command.getBody(), CacheExpireCommand.class);

        logger.info("received command : {}", cacheExpireCommand);

        cacheProxyFactory.getCacheProxy(cacheExpireCommand.getCacheType()).cacheExpire(cacheExpireCommand.getUpdateObj());
    }
}

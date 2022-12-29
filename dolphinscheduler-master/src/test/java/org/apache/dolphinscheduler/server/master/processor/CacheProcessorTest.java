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
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.remote.command.Command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.netty.channel.Channel;

/**
 * task ack processor test
 */
@ExtendWith(MockitoExtension.class)
public class CacheProcessorTest {

    @InjectMocks
    private CacheProcessor cacheProcessor = new CacheProcessor();

    @Mock
    private Channel channel;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @BeforeEach
    public void before() {
        Mockito.when(cacheManager.getCache(CacheType.TENANT.getCacheName())).thenReturn(cache);
    }

    @Test
    public void testProcess() {
        Tenant tenant = new Tenant();
        tenant.setId(1);
        CacheExpireCommand cacheExpireCommand = new CacheExpireCommand(CacheType.TENANT, "1");
        Command command = cacheExpireCommand.convert2Command();

        cacheProcessor.process(channel, command);
    }
}

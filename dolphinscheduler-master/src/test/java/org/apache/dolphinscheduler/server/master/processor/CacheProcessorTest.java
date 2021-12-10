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
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.netty.channel.Channel;

/**
 * task ack processor test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class CacheProcessorTest {

    private CacheProcessor cacheProcessor;

    @Mock
    private Channel channel;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Before
    public void before() {
        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(CacheManager.class)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(CacheType.TENANT.getCacheName())).thenReturn(cache);
        cacheProcessor = new CacheProcessor();
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

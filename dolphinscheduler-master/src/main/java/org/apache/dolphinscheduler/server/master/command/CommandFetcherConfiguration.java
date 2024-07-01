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

package org.apache.dolphinscheduler.server.master.command;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.server.master.config.CommandFetchStrategy;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterSlotManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandFetcherConfiguration {

    @Bean
    public ICommandFetcher commandFetcher(MasterConfig masterConfig,
                                          MasterSlotManager masterSlotManager,
                                          CommandDao commandDao) {
        CommandFetchStrategy commandFetchStrategy =
                checkNotNull(masterConfig.getCommandFetchStrategy(), "command fetch strategy is null");
        switch (commandFetchStrategy.getType()) {
            case ID_SLOT_BASED:
                CommandFetchStrategy.IdSlotBasedFetchConfig idSlotBasedFetchConfig =
                        (CommandFetchStrategy.IdSlotBasedFetchConfig) commandFetchStrategy.getConfig();
                return new IdSlotBasedCommandFetcher(idSlotBasedFetchConfig, masterSlotManager, commandDao);
            default:
                throw new IllegalArgumentException(
                        "unsupported command fetch strategy type: " + commandFetchStrategy.getType());
        }
    }
}

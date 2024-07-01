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

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.server.master.config.CommandFetchStrategy;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.registry.MasterSlotManager;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * The command fetcher which is fetch commands by command id and slot.
 */
@Slf4j
public class IdSlotBasedCommandFetcher implements ICommandFetcher {

    private final CommandFetchStrategy.IdSlotBasedFetchConfig idSlotBasedFetchConfig;

    private final CommandDao commandDao;

    private final MasterSlotManager masterSlotManager;

    public IdSlotBasedCommandFetcher(CommandFetchStrategy.IdSlotBasedFetchConfig idSlotBasedFetchConfig,
                                     MasterSlotManager masterSlotManager,
                                     CommandDao commandDao) {
        this.idSlotBasedFetchConfig = idSlotBasedFetchConfig;
        this.masterSlotManager = masterSlotManager;
        this.commandDao = commandDao;
    }

    @Override
    public List<Command> fetchCommands() {
        long scheduleStartTime = System.currentTimeMillis();
        int currentSlotIndex = masterSlotManager.getSlot();
        int totalSlot = masterSlotManager.getMasterSize();
        if (totalSlot <= 0 || currentSlotIndex < 0) {
            log.warn("Slot is validated, current master slots: {}, the current slot index is {}", totalSlot,
                    currentSlotIndex);
            return Collections.emptyList();
        }
        List<Command> commands = commandDao.queryCommandByIdSlot(
                currentSlotIndex,
                totalSlot,
                idSlotBasedFetchConfig.getIdStep(),
                idSlotBasedFetchConfig.getFetchSize());
        long cost = System.currentTimeMillis() - scheduleStartTime;
        log.info("Fetch commands: {} success, cost: {}ms, totalSlot: {}, currentSlotIndex: {}", commands.size(), cost,
                totalSlot, currentSlotIndex);
        ProcessInstanceMetrics.recordCommandQueryTime(cost);
        return commands;
    }

}

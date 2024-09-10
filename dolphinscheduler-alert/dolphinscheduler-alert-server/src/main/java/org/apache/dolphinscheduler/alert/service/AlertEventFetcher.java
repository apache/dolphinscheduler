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

package org.apache.dolphinscheduler.alert.service;

import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertEventFetcher extends AbstractEventFetcher<Alert> {

    private final AlertDao alertDao;

    public AlertEventFetcher(AlertHAServer alertHAServer,
                             AlertDao alertDao,
                             AlertEventPendingQueue alertEventPendingQueue) {
        super("AlertEventFetcher", alertHAServer, alertEventPendingQueue);
        this.alertDao = alertDao;
    }

    @Override
    public List<Alert> fetchPendingEvent(int eventOffset) {
        return alertDao.listPendingAlerts(eventOffset);
    }

    @Override
    protected int getEventOffset(Alert event) {
        return event.getId();
    }
}

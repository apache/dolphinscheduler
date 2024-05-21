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

import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.repository.ListenerEventDao;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListenerEventFetcher extends AbstractEventFetcher<ListenerEvent> {

    private final ListenerEventDao listenerEventDao;

    protected ListenerEventFetcher(AlertHAServer alertHAServer,
                                   ListenerEventDao listenerEventDao,
                                   ListenerEventPendingQueue listenerEventPendingQueue) {
        super("ListenerEventFetcher", alertHAServer, listenerEventPendingQueue);
        this.listenerEventDao = listenerEventDao;
    }

    @Override
    protected int getEventOffset(ListenerEvent event) {
        return event.getId();
    }

    @Override
    public List<ListenerEvent> fetchPendingEvent(int eventOffset) {
        return listenerEventDao.listingPendingEvents(eventOffset, FETCH_SIZE);
    }
}

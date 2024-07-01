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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ListenerEventDao;

import java.util.Date;
import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ListenerEventDaoImpl extends BaseDao<ListenerEvent, ListenerEventMapper> implements ListenerEventDao {

    public ListenerEventDaoImpl(@NonNull ListenerEventMapper listenerEventMapper) {
        super(listenerEventMapper);
    }

    @Override
    public List<ListenerEvent> listingPendingEvents(int minId, int limit) {
        return mybatisMapper.listingListenerEventByStatus(minId, AlertStatus.WAIT_EXECUTION.getCode(), limit);
    }

    @Override
    public void updateListenerEvent(int eventId, AlertStatus alertStatus, String message, Date date) {
        mybatisMapper.updateListenerEvent(eventId, alertStatus, message, date);
    }
}

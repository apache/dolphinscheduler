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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.repository.ListenerEventDao;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ListenerEventDaoImplTest extends BaseDaoTest {

    @Autowired
    private ListenerEventDao listenerEventDao;

    @Test
    void listingPendingEvents() {
        int minId = -1;
        int limit = 10;
        assertThat(listenerEventDao.listingPendingEvents(minId, limit)).isEmpty();

        ListenerEvent listenerEvent = ListenerEvent.builder()
                .eventType(ListenerEventType.SERVER_DOWN)
                .sign("test")
                .createTime(new Date())
                .updateTime(new Date())
                .postStatus(AlertStatus.WAIT_EXECUTION)
                .build();
        listenerEventDao.insert(listenerEvent);

        listenerEvent = ListenerEvent.builder()
                .eventType(ListenerEventType.SERVER_DOWN)
                .sign("test")
                .createTime(new Date())
                .updateTime(new Date())
                .postStatus(AlertStatus.EXECUTION_SUCCESS)
                .build();
        listenerEventDao.insert(listenerEvent);

        assertThat(listenerEventDao.listingPendingEvents(minId, limit)).hasSize(1);
    }

    @Test
    void updateListenerEvent() {
        ListenerEvent listenerEvent = ListenerEvent.builder()
                .eventType(ListenerEventType.SERVER_DOWN)
                .sign("test")
                .createTime(new Date())
                .updateTime(new Date())
                .postStatus(AlertStatus.WAIT_EXECUTION)
                .build();
        listenerEventDao.insert(listenerEvent);
        listenerEventDao.updateListenerEvent(listenerEvent.getId(), AlertStatus.EXECUTION_SUCCESS, "test", new Date());
        assertThat(listenerEventDao.queryById(listenerEvent.getId()).getPostStatus())
                .isEqualTo(AlertStatus.EXECUTION_SUCCESS);
    }
}

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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ServerDownListenerEvent;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;

/**
 * AlertPluginInstanceMapper mapper test
 */
public class ListenerEventMapperTest extends BaseDaoTest {

    @Autowired
    private ListenerEventMapper listenerEventMapper;

    /**
     * test insert
     *
     * @return
     */
    @Test
    public void testInsert() {
        ListenerEvent serverDownListenerEvent = generateServerDownListenerEvent("192.168.x.x");
        listenerEventMapper.insert(serverDownListenerEvent);
        Assertions.assertTrue(serverDownListenerEvent.getId() > 0);
    }

    /**
     * test batch insert
     *
     * @return
     */
    @Test
    public void testBatchInsert() {
        ListenerEvent event1 = generateServerDownListenerEvent("192.168.x.1");
        ListenerEvent event2 = generateServerDownListenerEvent("192.168.x.2");
        listenerEventMapper.batchInsert(Lists.newArrayList(event1, event2));
        Assertions.assertEquals(listenerEventMapper.selectCount(new QueryWrapper<>()), 2L);
    }

    /**
     * test list listener event by status
     *
     * @return
     */
    @Test
    public void testListingListenerEventByStatus() {
        ListenerEvent event1 = generateServerDownListenerEvent("192.168.x.1");
        ListenerEvent event2 = generateServerDownListenerEvent("192.168.x.2");
        listenerEventMapper.batchInsert(Lists.newArrayList(event1, event2));
        List<ListenerEvent> listenerEvents =
                listenerEventMapper.listingListenerEventByStatus(-1, AlertStatus.WAIT_EXECUTION.getCode(), 50);
        Assertions.assertEquals(listenerEvents.size(), 2);
    }

    /**
     * test update server down event
     *
     * @return
     */
    @Test
    public void testUpdateListenerEvent() {
        ListenerEvent event = generateServerDownListenerEvent("192.168.x.1");
        listenerEventMapper.insert(event);
        listenerEventMapper.updateListenerEvent(event.getId(), AlertStatus.EXECUTION_FAILURE, "fail", new Date());
        ListenerEvent updatedEvent = listenerEventMapper.selectById(event.getId());
        Assertions.assertEquals(updatedEvent.getPostStatus(), AlertStatus.EXECUTION_FAILURE);
        Assertions.assertEquals(updatedEvent.getLog(), "fail");
    }

    /**
     * test delete listener event
     */
    @Test
    public void testDeleteListenerEvent() {
        ListenerEvent event = generateServerDownListenerEvent("192.168.x.1");
        listenerEventMapper.insert(event);
        listenerEventMapper.deleteById(event);
        ListenerEvent actualAlert = listenerEventMapper.selectById(event.getId());
        Assertions.assertNull(actualAlert);
    }

    /**
     * create server down event
     *
     * @param host worker host
     * @return listener event
     */
    private ListenerEvent generateServerDownListenerEvent(String host) {
        ServerDownListenerEvent event = new ServerDownListenerEvent();
        event.setEventTime(new Date());
        event.setHost(host);
        event.setType("WORKER");
        ListenerEvent listenerEvent = new ListenerEvent();
        listenerEvent.setEventType(ListenerEventType.SERVER_DOWN);
        listenerEvent.setContent(JSONUtils.toJsonString(event));
        listenerEvent.setSign(DigestUtils.sha1Hex(listenerEvent.getContent()));
        listenerEvent.setLog("success");
        listenerEvent.setCreateTime(DateUtils.getCurrentDate());
        listenerEvent.setUpdateTime(DateUtils.getCurrentDate());
        listenerEvent.setPostStatus(AlertStatus.WAIT_EXECUTION);
        return listenerEvent;
    }
}

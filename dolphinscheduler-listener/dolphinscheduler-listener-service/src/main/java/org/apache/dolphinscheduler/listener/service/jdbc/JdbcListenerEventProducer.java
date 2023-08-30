/*
 *
 *  * Licensed to Apache Software Foundation (ASF) under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. Apache Software Foundation (ASF) licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.apache.dolphinscheduler.listener.service.jdbc;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostStatus;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.ListenerEvent;
import org.apache.dolphinscheduler.listener.service.ListenerEventProducer;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@ConditionalOnProperty(prefix = "listener", name = "type", havingValue = "jdbc")
@Slf4j
public class JdbcListenerEventProducer implements ListenerEventProducer {

    private final JdbcOperator jdbcOperator;

    public JdbcListenerEventProducer(JdbcOperator jdbcOperator) {
        this.jdbcOperator = jdbcOperator;
    }
    @Override
    public void save(ListenerEvent listenerEvent) {
        ListenerEventType listenerEventType = listenerEvent.getListenerEventType();
        List<ListenerPluginInstance> listenerPluginInstances =
                jdbcOperator.getListenerPluginInstanceByEventType(listenerEventType);
        if (CollectionUtils.isEmpty(listenerPluginInstances)) {
            return;
        }
        String content = JSONUtils.toJsonString(listenerEvent);
        List<JdbcListenerEvent> events = Lists.newArrayListWithExpectedSize(listenerPluginInstances.size());
        for (ListenerPluginInstance instance : listenerPluginInstances) {
            JdbcListenerEvent event = new JdbcListenerEvent();
            event.setContent(content);
            event.setEventType(listenerEventType);
            event.setPluginInstanceId(instance.getId());
            event.setPostStatus(ListenerEventPostStatus.WAIT_EXECUTION);
            event.setCreateTime(new Date());
            event.setUpdateTime(new Date());
            events.add(event);
        }
        jdbcOperator.batchInsertListenerEvent(events);
    }
}

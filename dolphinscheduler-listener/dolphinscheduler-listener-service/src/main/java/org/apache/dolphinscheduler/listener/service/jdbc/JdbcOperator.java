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

import static java.util.stream.Collectors.toSet;

import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.dao.mapper.ListenerPluginInstanceMapper;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.service.jdbc.mapper.ListenerEventMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Component
@ConditionalOnProperty(prefix = "listener", name = "type", havingValue = "jdbc")
public class JdbcOperator {

    @Autowired
    private ListenerEventMapper listenerEventMapper;
    @Autowired
    private ListenerPluginInstanceMapper listenerPluginInstanceMapper;

    public List<JdbcListenerEvent> getJdbcListenerEventListByInstanceId(int listenerInstanceId) {
        LambdaQueryWrapper<JdbcListenerEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JdbcListenerEvent::getPluginInstanceId, listenerInstanceId)
                .last("limit 100");
        List<JdbcListenerEvent> eventList = listenerEventMapper.selectList(wrapper);
        return eventList;
    }

    public void updateListenerEvent(JdbcListenerEvent jdbcListenerEvent) {
        listenerEventMapper.updateById(jdbcListenerEvent);
    }

    public void deleteListenerEvent(JdbcListenerEvent jdbcListenerEvent) {
        listenerEventMapper.deleteById(jdbcListenerEvent);
    }

    public List<ListenerPluginInstance> getListenerPluginInstanceByEventType(ListenerEventType listenerEventType) {
        LambdaQueryWrapper<ListenerPluginInstance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ListenerPluginInstance::getId, ListenerPluginInstance::getListenerEventTypes);
        List<ListenerPluginInstance> listenerPluginInstances =
                listenerPluginInstanceMapper.selectList(queryWrapper)
                        .stream()
                        .filter(x -> Arrays.stream(x.getListenerEventTypes().split(","))
                                .map(Integer::parseInt).collect(toSet())
                                .contains(listenerEventType.getCode()))
                        .collect(Collectors.toList());
        return listenerPluginInstances;
    }

    public void batchInsertListenerEvent(List<JdbcListenerEvent> jdbcListenerEvents) {
        listenerEventMapper.batchInsert(jdbcListenerEvents);
    }

}

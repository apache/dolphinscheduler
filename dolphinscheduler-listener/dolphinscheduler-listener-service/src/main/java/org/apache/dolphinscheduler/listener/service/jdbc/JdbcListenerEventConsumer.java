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

import org.apache.dolphinscheduler.listener.service.ListenerEventConsumer;

import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "listener", name = "type", havingValue = "jdbc")
@Slf4j
public class JdbcListenerEventConsumer implements ListenerEventConsumer {

    private final JdbcOperator jdbcOperator;

    public JdbcListenerEventConsumer(JdbcOperator jdbcOperator) {
        this.jdbcOperator = jdbcOperator;
    }
    @PostConstruct
    public void init() {
        log.info("Init jJdbc Listener Consumer");
    }
    @Override
    public List<JdbcListenerEvent> take(int listenerInstanceId) {
        return jdbcOperator.getJdbcListenerEventListByInstanceId(listenerInstanceId);
    }

    @Override
    public void update(JdbcListenerEvent jdbcListenerEvent) {
        jdbcOperator.updateListenerEvent(jdbcListenerEvent);
    }

    @Override
    public void delete(JdbcListenerEvent jdbcListenerEvent) {
        jdbcOperator.deleteListenerEvent(jdbcListenerEvent);
    }
}

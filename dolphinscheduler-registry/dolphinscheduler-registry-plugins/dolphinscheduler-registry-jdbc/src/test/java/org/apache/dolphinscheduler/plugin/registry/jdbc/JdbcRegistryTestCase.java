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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.IJdbcRegistryServer;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import java.util.concurrent.atomic.AtomicReference;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.google.common.truth.Truth;

@SpringBootTest(classes = {JdbcRegistryProperties.class})
@SpringBootApplication(scanBasePackageClasses = JdbcRegistryProperties.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class JdbcRegistryTestCase extends RegistryTestCase<JdbcRegistry> {

    @Autowired
    private JdbcRegistryProperties jdbcRegistryProperties;

    @Autowired
    private IJdbcRegistryServer jdbcRegistryServer;

    @SneakyThrows
    @Test
    public void testAddConnectionStateListener() {

        AtomicReference<ConnectionState> connectionState = new AtomicReference<>();
        registry.addConnectionStateListener(connectionState::set);

        // todo: since the jdbc registry is started at the auto configuration, the stateListener is added after the
        // registry is started.
        Truth.assertThat(connectionState.get()).isNull();
    }

    @Override
    public JdbcRegistry createRegistry() {
        return new JdbcRegistry(jdbcRegistryProperties, jdbcRegistryServer);
    }

}

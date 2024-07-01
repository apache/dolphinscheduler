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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryLock;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {JdbcRegistryProperties.class})
@SpringBootApplication(scanBasePackageClasses = JdbcRegistryProperties.class)
public abstract class JdbcRegistryTestCase extends RegistryTestCase<JdbcRegistry> {

    @Autowired
    private JdbcRegistryProperties jdbcRegistryProperties;

    @Autowired
    private JdbcOperator jdbcOperator;

    @Test
    @SneakyThrows
    public void testTryToAcquireLock_lockIsAlreadyBeenAcquired() {
        final String lockKey = "testTryToAcquireLock_lockIsAlreadyBeenAcquired";
        // acquire success
        JdbcRegistryLock jdbcRegistryLock = jdbcOperator.tryToAcquireLock(lockKey);
        // acquire failed
        assertThat(jdbcOperator.tryToAcquireLock(lockKey)).isNull();
        // release
        jdbcOperator.releaseLock(jdbcRegistryLock.getId());
    }

    @Override
    public JdbcRegistry createRegistry() {
        return new JdbcRegistry(jdbcRegistryProperties, jdbcOperator);
    }

}

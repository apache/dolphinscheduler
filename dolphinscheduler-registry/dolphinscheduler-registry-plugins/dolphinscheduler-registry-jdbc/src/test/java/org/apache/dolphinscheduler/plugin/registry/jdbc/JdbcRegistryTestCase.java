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

    @Override
    public JdbcRegistry createRegistry() {
        return new JdbcRegistry(jdbcRegistryProperties, jdbcOperator);
    }

}

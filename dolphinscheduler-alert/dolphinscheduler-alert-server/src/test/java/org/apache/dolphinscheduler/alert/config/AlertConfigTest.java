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

package org.apache.dolphinscheduler.alert.config;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.extract.base.utils.Host;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;

@AutoConfigureMockMvc
@SpringBootTest(classes = AlertConfig.class)
class AlertConfigTest {

    @Autowired
    private AlertConfig alertConfig;

    @Test
    void testValidate() {
        assertThat(alertConfig.getWaitTimeout()).isEqualTo(10);
        assertThat(alertConfig.getSenderParallelism()).isEqualTo(101);
    }

    @Test
    void defaultAddressUsesRpcPort() {
        AlertConfig config = new AlertConfig();
        config.setPort(50052);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(config, "alertConfig");
        config.validate(config, errors);
        assertThat(errors.hasErrors()).isFalse();
        assertThat(Host.of(config.getAlertServerAddress()).getPort()).isEqualTo(50052);
    }

    @Test
    void acceptsAdvertisedHostWithRpcPort() {
        AlertConfig config = new AlertConfig();
        config.setPort(50052);
        config.setAlertServerAddress("advertised.example:50052");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(config, "alertConfig");
        config.validate(config, errors);
        assertThat(errors.hasErrors()).isFalse();
        assertThat(config.getAlertServerAddress()).isEqualTo("advertised.example:50052");
    }

    @Test
    void rejectsAdvertisedPortDifferentFromRpcPort() {
        AlertConfig config = new AlertConfig();
        config.setPort(50052);
        config.setAlertServerAddress("advertised.example:50053");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(config, "alertConfig");
        config.validate(config, errors);
        assertThat(errors.hasFieldErrors("alertServerAddress")).isTrue();
    }

}

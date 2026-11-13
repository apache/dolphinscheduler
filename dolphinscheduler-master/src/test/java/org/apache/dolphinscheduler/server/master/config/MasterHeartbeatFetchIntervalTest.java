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

package org.apache.dolphinscheduler.server.master.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.validation.BeanPropertyBindingResult;

class MasterHeartbeatFetchIntervalTest {

    @Test
    void bindsHeartbeatFetchIntervalFromConfiguration() {
        Binder binder = new Binder(new MapConfigurationPropertySource(
                Collections.singletonMap("master.heartbeat-fetch-interval", "250ms")));
        MasterConfig config = binder.bind("master", Bindable.of(MasterConfig.class)).get();
        assertEquals(Duration.ofMillis(250), config.getHeartbeatFetchInterval());
    }

    @Test
    void rejectsNonPositiveHeartbeatFetchInterval() {
        for (Duration interval : new Duration[]{Duration.ZERO, Duration.ofMillis(-1)}) {
            MasterConfig config = new MasterConfig();
            config.setHeartbeatFetchInterval(interval);
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(config, "master");
            config.validate(config, errors);
            assertTrue(errors.hasFieldErrors("heartbeatFetchInterval"));
        }
    }
}

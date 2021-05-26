package org.apache.dolphinscheduler.plugin.register.zookeeper;/*
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

import java.util.function.Function;

public enum ZookeeperConfiguration {

    NAME_SPACE("nameSpace", "dolphinscheduler", value -> value),
    SERVERS("servers", null, value -> value),

    /**
     * Initial amount of time to wait between retries
     */
    BASE_SLEEP_TIME("baseSleepTimeMs", 60, Integer::valueOf),
    MAX_SLEEP_TIME("maxSleepMs", 300, Integer::valueOf),
    DIGEST("digest", null, value -> value),

    MAX_RETRIES("maxRetries", 5, Integer::valueOf),


    //todo
    SESSION_TIMEOUT_MS("sessionTimeoutMS", 1000, Integer::valueOf),
    CONNECTION_TIMEOUT_MS("connectionTimeoutMS", 1000, Integer::valueOf),

    BLOCK_UNTIL_CONNECTED_WAIT_MS("blockUntilConnectedWait", 600, Integer::valueOf),
    ;
    private final String name;

    public String getName() {
        return name;
    }

    private final Object defaultValue;

    private final Function<String, Object> converter;

    <T> ZookeeperConfiguration(String name, T defaultValue, Function<String, T> converter) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.converter = (Function<String, Object>) converter;
    }

    public <T> T getParameterValue(String param) {
        Object value = param != null ? converter.apply(param) : defaultValue;
        return (T) value;
    }

}

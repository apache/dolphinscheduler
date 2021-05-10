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

import java.util.Map;

public class ZookeeperConfiguration {

    public static final String HOSTS_NAME = "hosts";

    public static final String NAMESPACE_NAME = "namespace";

    public static final String MAX_RETRIES_NAME = "max.retries";

    public static final String MAX_SLEEP_TIME_MILLI_SECONDS_NAME = "max.sleep.time";

    public static final String SERVERS_NAME = "servers";


    public static String HOSTS;

    public static String NAMESPACE;

    public static int MAX_RETRIES;

    public static int MAX_SLEEP_TIME_MILLI_SECONDS;

    public static String SERVERS;

    public static void initConfiguration(Map<String, Object> config) {
        //assert null and set default
        SERVERS = config.get(SERVERS_NAME).toString();
        HOSTS = config.get(HOSTS_NAME).toString();
        NAMESPACE = config.get(NAMESPACE_NAME).toString();
        MAX_RETRIES = (int) config.get(MAX_RETRIES_NAME);
        MAX_SLEEP_TIME_MILLI_SECONDS = (int) config.get(MAX_SLEEP_TIME_MILLI_SECONDS_NAME);


    }
}

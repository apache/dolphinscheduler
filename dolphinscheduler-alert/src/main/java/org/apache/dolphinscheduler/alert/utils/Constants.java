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

package org.apache.dolphinscheduler.alert.utils;

/**
 * constants
 */
public class Constants {
    /**
     * alert properties path
     */
    public static final String ALERT_PROPERTIES_PATH = "/alert.properties";
    /**
     * zookeeper properties path
     */
    public static final String ZOOKEEPER_PROPERTIES_PATH = "/zookeeper.properties";
    /**
     * default alert plugin dir
     **/
    public static final String ALERT_PLUGIN_PATH = "./lib/plugin/alert";
    public static final int ALERT_SCAN_INTERVAL = 5000;

    public static final String ZOOKEEPER_LIST = "zookeeper.quorum";
    public static final String ZOOKEEPER_BASE_SLEEP_TIME_MS = "zookeeper.retry.base.sleep";
    public static final String ZOOKEEPER_MAX_SLEEP_MS = "zookeeper.retry.max.sleep";
    public static final String ZOOKEEPER_MAX_RETRY = "zookeeper.retry.maxtime";
    public static final String ZOOKEEPER_SESSION_TIMEOUT_MS = "zookeeper.session.timeout";
    public static final String ZOOKEEPER_CONNECTION_TIMEOUT_MS = "zookeeper.connection.timeout";
    public static final String ZOOKEEPER_DIGEST = "zookeeper.connection.digest";
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS = "/lock/alerts";
    public static final String ZOOKEEPER_ROOT = "zookeeper.dolphinscheduler.root";
    public static final String ZOOKEEPER_ABNORMAL_TOLERATING_NUMBER = "zookeeper.abnormal.tolerating.number";

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}

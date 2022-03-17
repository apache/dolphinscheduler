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

package org.apache.dolphinscheduler.sdk;

import org.apache.dolphinscheduler.sdk.tea.Common;
import org.apache.dolphinscheduler.sdk.tea.TeaConverter;
import org.apache.dolphinscheduler.sdk.tea.TeaException;
import org.apache.dolphinscheduler.sdk.tea.TeaPair;

/**
 * DAG Scheduler SDK Client Helper
 * @author zhaowei,ouyangyewei
 */
public class DSClientHelper {
    /**
     * Default communication protocol
     */
    public static final String DEFAULT_PROTOCOL = "HTTP";
    /**
     * Default number of idle connections
     */
    public static final Integer DEFAULT_MAX_IDLE_CONNECTIONS = 5;
    /**
     * Default request read wait time (ms)
     */
    public static final Integer DEFAULT_READ_TIMEOUT_IN_MILLIS = 2000;
    /**
     * Default connection timeout (ms)
     */
    public static final Integer DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 1000;

    /**
     * Build SDK Client, The connection pool reuses connections with the same endpoint(ip:port)
     * @param config
     * @return
     */
    private static DSClient getClient(final Config config) {
        if (null == config.getMaxIdleConns()) {
            config.setMaxIdleConns(DEFAULT_MAX_IDLE_CONNECTIONS);
        }
        if (null == config.getReadTimeout()) {
            config.setReadTimeout(DEFAULT_READ_TIMEOUT_IN_MILLIS);
        }
        if (null == config.getConnectTimeout()) {
            config.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS);
        }
        if (Common.empty(config.getProtocol())) {
            config.setProtocol(DEFAULT_PROTOCOL);
        }

        // validation
        if (Common.empty(config.getToken())) {
            throw new TeaException(TeaConverter.buildMap(
                    new TeaPair("code", "MissingParameter"),
                    new TeaPair("message", "The parameter 'token' that is mandatory for processing this request is not supplied.")
            ));
        }
        if (Common.empty(config.getEndpoint())) {
            throw new TeaException(TeaConverter.buildMap(
                    new TeaPair("code", "MissingParameter"),
                    new TeaPair("message", "The parameter 'endpoint' that is mandatory for processing this request is not supplied.")
            ));
        }
        return new DSClient(config);
    }

    /**
     * Build SDK Client
     * @param token
     * @param endpoint
     * @return
     */
    public static DSClient getClient(final String token, final String endpoint) {
        Config config = new Config().setToken(token).setEndpoint(endpoint);
        return getClient(config);
    }
}

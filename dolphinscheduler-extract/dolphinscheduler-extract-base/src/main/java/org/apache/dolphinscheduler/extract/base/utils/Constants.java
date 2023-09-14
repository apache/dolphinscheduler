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

package org.apache.dolphinscheduler.extract.base.utils;

import org.apache.dolphinscheduler.common.utils.NetUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * constant
 */
public class Constants {

    private Constants() {
        throw new IllegalStateException(Constants.class.getName());
    }

    public static final String COMMA = ",";

    public static final String SLASH = "/";

    public static final int NETTY_SERVER_HEART_BEAT_TIME = 1000 * 60 * 3 + 1000;

    public static final int NETTY_CLIENT_HEART_BEAT_TIME = 1000 * 6;

    /**
     * charset
     */
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * cpus
     */
    public static final int CPUS = Runtime.getRuntime().availableProcessors();

    public static final String LOCAL_ADDRESS = NetUtils.getHost();

    /**
     * OS Name
     */
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * warm up time
     */
    public static final int WARM_UP_TIME = 10 * 60 * 1000;

}

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

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;

import java.util.Map;

public class RuntimeOptions extends TeaModel {
    @NameInMap("autoretry")
    public Boolean autoretry = false;
    @NameInMap("ignoreSSL")
    public Boolean ignoreSSL = true;
    @NameInMap("max_attempts")
    public Integer maxAttempts;
    @NameInMap("backoff_policy")
    public String backoffPolicy;
    @NameInMap("backoff_period")
    public Integer backoffPeriod;
    @NameInMap("readTimeout")
    public Integer readTimeout;
    @NameInMap("connectTimeout")
    public Integer connectTimeout;
    @NameInMap("httpProxy")
    public String httpProxy;
    @NameInMap("httpsProxy")
    public String httpsProxy;
    @NameInMap("noProxy")
    public String noProxy;
    @NameInMap("maxIdleConns")
    public Integer maxIdleConns;

    public RuntimeOptions() {
    }

    /**
     * If RuntimeOptions set httpProxy or httpsProxy, it will get new http client
     */
    public static RuntimeOptions build(Map<String, ?> map) throws Exception {
        RuntimeOptions self = new RuntimeOptions();
        return (RuntimeOptions) TeaModel.build(map, self);
    }
}


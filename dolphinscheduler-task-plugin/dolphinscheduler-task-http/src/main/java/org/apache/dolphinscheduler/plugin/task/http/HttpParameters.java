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

package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * http parameter
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpParameters extends AbstractParameters {

    private String url;

    @JsonProperty("httpMethod")
    private HttpRequestMethod httpRequestMethod;

    @JsonProperty("httpParams")
    private List<HttpProperty> httpRequestParams;

    @JsonProperty("httpBody")
    private String httpRequestBody;

    private HttpCheckCondition httpCheckCondition = HttpCheckCondition.STATUS_CODE_DEFAULT;

    private String condition;

    /**
     * Connect Timeout
     * Unit: ms
     */
    private int connectTimeout;

    /**
     * Whether to enable TCP keepalive (SO_KEEPALIVE) on the request socket.
     * <p>
     * Useful when the backend is reachable through a stateful firewall / NAT /
     * security group that evicts idle TCP sessions: keepalive probes refresh the
     * conntrack entry while the worker is waiting on a long-lived response. Note the
     * probe interval itself is governed by the worker host OS sysctls
     * ({@code net.ipv4.tcp_keepalive_time} etc), not by this JVM setting.
     */
    private boolean socketKeepAlive;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(url) && httpRequestMethod != null
                && connectTimeout > 0;
    }

}

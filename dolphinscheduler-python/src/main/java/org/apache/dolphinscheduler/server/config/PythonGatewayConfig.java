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

package org.apache.dolphinscheduler.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("python-gateway")
public class PythonGatewayConfig {
    private String gatewayServerAddress;
    private int gatewayServerPort;
    private String pythonAddress;
    private int pythonPort;
    private int connectTimeout;
    private int readTimeout;

    public String getGatewayServerAddress() {
        return gatewayServerAddress;
    }

    public void setGatewayServerAddress(String gatewayServerAddress) {
        this.gatewayServerAddress = gatewayServerAddress;
    }

    public int getGatewayServerPort() {
        return gatewayServerPort;
    }

    public void setGatewayServerPort(int gatewayServerPort) {
        this.gatewayServerPort = gatewayServerPort;
    }

    public String getPythonAddress() {
        return pythonAddress;
    }

    public void setPythonAddress(String pythonAddress) {
        this.pythonAddress = pythonAddress;
    }

    public int getPythonPort() {
        return pythonPort;
    }

    public void setPythonPort(int pythonPort) {
        this.pythonPort = pythonPort;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}

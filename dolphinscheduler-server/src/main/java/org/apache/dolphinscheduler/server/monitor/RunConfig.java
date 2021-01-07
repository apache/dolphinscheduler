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
package org.apache.dolphinscheduler.server.monitor;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * zookeeper conf
 */
@Component
@PropertySource("classpath:config/install_config.conf")
public class RunConfig {

    //zk connect config
    @Value("${masters}")
    private String masters;

    @Value("${workers}")
    private String workers;

    @Value("${alertServer}")
    private String alertServer;

    @Value("${apiServers}")
    private String apiServers;

    @Value("${sshPort}")
    private String sshPort;

    public String getMasters() {
        return masters;
    }

    public void setMasters(String masters) {
        this.masters = masters;
    }

    public String getWorkers() {
        StringBuilder sb = new StringBuilder(50);
        if(StringUtils.isNotBlank(workers)){
            String[] workersArr = workers.trim().split(Constants.COMMA);
            for (String workerGroup : workersArr) {
                sb.append(workerGroup.split(Constants.COLON)[0]).append(Constants.COMMA);
            }
        }
        if( sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public void setWorkers(String workers) {
        this.workers = workers;
    }

    public String getAlertServer() {
        return alertServer;
    }

    public void setAlertServer(String alertServer) {
        this.alertServer = alertServer;
    }

    public String getApiServers() {
        return apiServers;
    }

    public void setApiServers(String apiServers) {
        this.apiServers = apiServers;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }
}
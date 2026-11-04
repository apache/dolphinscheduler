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

package org.apache.dolphinscheduler.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Stable identity stored in the registry for one server process. */
@Data
@NoArgsConstructor
public class ServerRegistration {

    private String host;
    private int port;
    private int processId;
    private long startupTime;

    public static ServerRegistration fromHeartBeat(BaseHeartBeat heartBeat) {
        ServerRegistration registration = new ServerRegistration();
        registration.setHost(heartBeat.getHost());
        registration.setPort(heartBeat.getPort());
        registration.setProcessId(heartBeat.getProcessId());
        registration.setStartupTime(heartBeat.getStartupTime());
        return registration;
    }

    public String getAddress() {
        return host + ":" + port;
    }

    public boolean matches(BaseHeartBeat heartBeat) {
        return heartBeat != null && processId == heartBeat.getProcessId()
                && startupTime == heartBeat.getStartupTime()
                && getAddress().equals(heartBeat.getHost() + ":" + heartBeat.getPort());
    }
}

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

package org.apache.dolphinscheduler.remote.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.COLON;

import java.io.Serializable;

import lombok.Data;
import lombok.NonNull;

@Data
public class Host implements Serializable {

    public static final Host EMPTY = new Host();

    private String ip;

    private int port;

    public Host() {
    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Host(String address) {
        int lastColonIndex = address.lastIndexOf(COLON);
        if (lastColonIndex < 0) {
            throw new IllegalArgumentException(String.format("Host : %s illegal.", address));
        }
        this.ip = address.substring(0, lastColonIndex);
        this.port = Integer.parseInt(address.substring(lastColonIndex + 1));
    }

    public String getAddress() {
        return ip + COLON + port;
    }

    public static Host of(@NonNull String address) {
        return new Host(address);
    }

}

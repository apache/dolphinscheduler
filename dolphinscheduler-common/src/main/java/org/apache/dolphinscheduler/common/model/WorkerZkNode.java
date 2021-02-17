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

/**
 * worker zookeeper node
 */
public class WorkerZkNode {

    /** worker host */
    private String addressHost;
    /** worker port */
    private String addressPort;
    /** worker weight */
    private String weight;
    /** worker registry startTime */
    private String startTime;

    public WorkerZkNode(String addressHost, String addressPort, String weight, String startTime) {
        this.addressHost = addressHost;
        this.addressPort = addressPort;
        this.weight = weight;
        this.startTime = startTime;
    }

    public String getAddressHost() {
        return addressHost;
    }

    public String getAddressPort() {
        return addressPort;
    }

    public String getWeight() {
        return weight;
    }

    public String getStartTime() {
        return startTime;
    }
}

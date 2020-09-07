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

import java.io.Serializable;
import java.util.Objects;

/**
 * server address
 */
public class Host implements Serializable {

    /**
     * address
     */
    private String address;

    /**
     * ip
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * weight
     */
    private int weight;

    /**
     * workGroup
     */
    private String workGroup;

    public Host() {
    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.address = ip + ":" + port;
    }

    public Host(String ip, int port, int weight) {
        this.ip = ip;
        this.port = port;
        this.address = ip + ":" + port;
        this.weight = weight;
    }

    public Host(String ip, int port, int weight,String workGroup) {
        this.ip = ip;
        this.port = port;
        this.address = ip + ":" + port;
        this.weight = weight;
        this.workGroup=workGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        this.address = ip + ":" + port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        this.address = ip + ":" + port;
    }

    public String getWorkGroup() {
        return workGroup;
    }

    public void setWorkGroup(String workGroup) {
        this.workGroup = workGroup;
    }

    /**
     * address convert host
     *
     * @param address address
     * @return host
     */
    public static Host of(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Host : address is null.");
        }
        String[] parts = address.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format("Host : %s illegal.", address));
        }
        Host host = null;
        if (parts.length == 2) {
            host = new Host(parts[0], Integer.parseInt(parts[1]));
        }
        if (parts.length == 3) {
            host = new Host(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        return host;
    }

    /**
     * whether old version
     *
     * @param address address
     * @return old version is true , otherwise is false
     */
    public static Boolean isOldVersion(String address) {
        String[] parts = address.split(":");
        return parts.length != 2 && parts.length != 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Host host = (Host) o;
        return Objects.equals(getAddress(), host.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }

    @Override
    public String toString() {
        return "Host{" +
                "address='" + address + '\'' +
                '}';
    }
}

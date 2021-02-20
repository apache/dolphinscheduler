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

import static org.apache.dolphinscheduler.common.Constants.COLON;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

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
     * startTime
     */
    private long startTime;

    /**
     * workGroup
     */
    private String workGroup;

    public Host() {
    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.address = ip + COLON + port;
    }

    public Host(String ip, int port, int weight, long startTime) {
        this.ip = ip;
        this.port = port;
        this.address = ip + COLON + port;
        this.weight = getWarmUpWeight(weight, startTime);
        this.startTime = startTime;
    }

    public Host(String ip, int port, int weight, long startTime, String workGroup) {
        this.ip = ip;
        this.port = port;
        this.address = ip + COLON + port;
        this.weight = getWarmUpWeight(weight, startTime);
        this.workGroup = workGroup;
        this.startTime = startTime;
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
        this.address = ip + COLON + port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        this.address = ip + COLON + port;
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
        String[] parts = address.split(COLON);
        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format("Host : %s illegal.", address));
        }
        Host host = null;
        if (parts.length == 2) {
            host = new Host(parts[0], Integer.parseInt(parts[1]));
        }
        if (parts.length == 4) {
            host = new Host(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Long.parseLong(parts[3]));
        }
        return host;
    }

    /**
     * generate host string
     * @param address address
     * @param weight weight
     * @param startTime startTime
     * @return address:weight:startTime
     */
    public static String generate(String address, int weight, long startTime) {
        StringJoiner stringJoiner = new StringJoiner(COLON);
        stringJoiner.add(address)
                .add(String.valueOf(weight))
                .add(String.valueOf(startTime));
        return stringJoiner.toString();
    }

    /**
     * whether old version
     *
     * @param address address
     * @return old version is true , otherwise is false
     */
    public static Boolean isOldVersion(String address) {
        String[] parts = address.split(COLON);
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
        return "Host{"
                + "address='" + address + '\''
                + ", weight=" + weight
                + ", startTime=" + startTime
                + ", workGroup='" + workGroup + '\''
                + '}';
    }

    /**
     * warm up
     */
    private int getWarmUpWeight(int weight, long startTime) {
        long uptime = System.currentTimeMillis() - startTime;
        //If the warm-up is not over, reduce the weight
        if (uptime > 0 && uptime < Constants.WARM_UP_TIME) {
            return (int) (weight * ((float) uptime / Constants.WARM_UP_TIME));
        }
        return weight;
    }

    /**
     * get address and weight
     *
     * @return address:weight
     */
    public String getAddressAndWeight() {
        return address + COLON + weight;
    }
}

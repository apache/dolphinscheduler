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

/**
 * server address
 */
public class Host implements Serializable {

    public static final Host EMPTY = new Host();

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

    public Host() {
    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.address = ip + COLON + port;
    }

    public Host(String address) {
        String[] parts = splitAddress(address);
        this.ip = parts[0];
        this.port = Integer.parseInt(parts[1]);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        String[] parts = splitAddress(address);
        this.ip = parts[0];
        this.port = Integer.parseInt(parts[1]);
        this.address = address;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        this.address = ip + COLON + port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        this.address = ip + COLON + port;
    }

    /**
     * address convert host
     *
     * @param address address
     * @return host
     */
    public static Host of(String address) {
        String[] parts = splitAddress(address);
        return new Host(parts[0], Integer.parseInt(parts[1]));
    }

    /**
     * address convert host
     *
     * @param address address
     * @return host
     */
    public static String[] splitAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Host : address is null.");
        }
        String[] parts = address.split(COLON);
        if (parts.length != 2) {
            throw new IllegalArgumentException(String.format("Host : %s illegal.", address));
        }
        return parts;
    }

    /**
     * whether old version
     *
     * @param address address
     * @return old version is true , otherwise is false
     */
    public static Boolean isOldVersion(String address) {
        String[] parts = address.split(COLON);
        return parts.length != 2;
    }

    @Override
    public String toString() {
        return "Host{"
                + "address='" + address + '\''
                + ", ip='" + ip + '\''
                + ", port=" + port
                + '}';
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
        return port == host.port && Objects.equals(address, host.address) && Objects.equals(ip, host.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, ip, port);
    }
}

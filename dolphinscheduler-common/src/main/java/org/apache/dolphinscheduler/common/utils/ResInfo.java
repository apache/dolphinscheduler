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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.Server;

import org.apache.commons.lang.StringUtils;

/**
 *  heartbeat for ZK reigster res info
 */
public class ResInfo {

    /**
     *  cpuUsage
     */
    private double cpuUsage;

    /**
     *  memoryUsage
     */
    private double memoryUsage;

    /**
     * loadAverage
     */
    private double loadAverage;

    public ResInfo(double cpuUsage, double memoryUsage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    public ResInfo(double cpuUsage, double memoryUsage, double loadAverage) {
        this(cpuUsage,memoryUsage);
        this.loadAverage = loadAverage;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    /**
     * get CPU and memory usage
     * @param cpuUsage cpu usage
     * @param memoryUsage memory usage
     * @param loadAverage load average
     * @return cpu and memory usage
     */
    public static String getResInfoJson(double cpuUsage, double memoryUsage, double loadAverage) {
        ResInfo resInfo = new ResInfo(cpuUsage,memoryUsage,loadAverage);
        return JSONUtils.toJsonString(resInfo);
    }

    /**
     * parse heartbeat info for zk
     * @param heartBeatInfo heartbeat info
     * @return heartbeat info to Server
     */
    public static Server parseHeartbeatForRegistryInfo(String heartBeatInfo) {
        if (!isValidHeartbeatForRegistryInfo(heartBeatInfo)) {
            return null;
        }
        String[] parts = heartBeatInfo.split(Constants.COMMA);
        Server server = new Server();
        server.setResInfo(getResInfoJson(Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])));
        server.setCreateTime(DateUtils.stringToDate(parts[6]));
        server.setLastHeartbeatTime(DateUtils.stringToDate(parts[7]));
        //set process id
        server.setId(Integer.parseInt(parts[9]));
        return server;
    }

    /**
     * is valid heartbeat info for zk
     * @param heartBeatInfo heartbeat info
     * @return heartbeat info is valid
     */
    public static boolean isValidHeartbeatForRegistryInfo(String heartBeatInfo) {
        if (!StringUtils.isEmpty(heartBeatInfo)) {
            String[] parts = heartBeatInfo.split(Constants.COMMA);
            return parts.length == Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH
                    || parts.length == Constants.HEARTBEAT_WITH_WEIGHT_FOR_ZOOKEEPER_INFO_LENGTH;
        }
        return false;
    }

    /**
     * is new heartbeat info for zk with weight
     * @param parts heartbeat info parts
     * @return heartbeat info is new with weight
     */
    public static boolean isNewHeartbeatWithWeight(String[] parts) {
        return parts.length == Constants.HEARTBEAT_WITH_WEIGHT_FOR_ZOOKEEPER_INFO_LENGTH;
    }

}

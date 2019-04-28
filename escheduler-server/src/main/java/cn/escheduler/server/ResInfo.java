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
package cn.escheduler.server;

import cn.escheduler.common.Constants;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.OSUtils;

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

    public ResInfo(){}

    public ResInfo(double cpuUsage , double memoryUsage){
        this.cpuUsage = cpuUsage ;
        this.memoryUsage = memoryUsage;
    }

    public ResInfo(double cpuUsage, double memoryUsage, double loadAverage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
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
     * add cpu load average by lidong for service monitor
     * @return
     */
    public static String getResInfoJson(){
        ResInfo resInfo = new ResInfo(OSUtils.cpuUsage(), OSUtils.memoryUsage(),OSUtils.loadAverage());
        return JSONUtils.toJson(resInfo);
    }


    /**
     * get CPU and memory usage
     * @return
     */
    public static String getResInfoJson(double cpuUsage , double memoryUsage){
        ResInfo resInfo = new ResInfo(cpuUsage,memoryUsage);
        return JSONUtils.toJson(resInfo);
    }


    /**
     * build heartbeat info for zk
     * @param host
     * @param port
     * @param cpuUsage
     * @param memoryUsage
     * @param createTime
     * @param lastHeartbeatTime
     * @return
     */
    public static String buildHeartbeatForZKInfo(String host , int port ,
                                         double cpuUsage , double memoryUsage,
                                         String createTime,String lastHeartbeatTime){

        return host + Constants.COMMA + port + Constants.COMMA
                + cpuUsage + Constants.COMMA
                + memoryUsage + Constants.COMMA
                + createTime + Constants.COMMA
                + lastHeartbeatTime;
    }

}

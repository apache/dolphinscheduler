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
package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

/**
 * monitor record for zookeeper
 */
public class ZookeeperRecord {

    /**
     * hostname
     */
    private String hostname;

    /**
     * connections
     */
    private int connections;

    /**
     * max connections
     */
    private int watches;

    /**
     * sent
     */
    private long sent;

    /**
     * received
     */
    private long received;

    /**
     * mode: leader or follower
     */
    private String mode;

    /**
     * min Latency
     */
    private float minLatency;

    /**
     * avg Latency
     */
    private float avgLatency;

    /**
     * max Latency
     */
    private float maxLatency;

    /**
     * node count
     */
    private int nodeCount;

    /**
     * date
     */
    private Date date;


    /**
     * is normal or not, 1:normal
     */
    private int state;


    public ZookeeperRecord(String hostname,int connections, int watches, long sent, long received, String mode, float minLatency, float avgLatency, float maxLatency, int nodeCount, int state,Date date) {
        this.hostname = hostname;
        this.connections = connections;
        this.watches = watches;
        this.sent = sent;
        this.received = received;
        this.mode = mode;
        this.minLatency = minLatency;
        this.avgLatency = avgLatency;
        this.maxLatency = maxLatency;
        this.nodeCount = nodeCount;
        this.state = state;
        this.date = date;
    }


    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getWatches() {
        return watches;
    }

    public void setWatches(int watches) {
        this.watches = watches;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public long getReceived() {
        return received;
    }

    public void setReceived(long received) {
        this.received = received;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public float getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(float minLatency) {
        this.minLatency = minLatency;
    }

    public float getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(float avgLatency) {
        this.avgLatency = avgLatency;
    }

    public float getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(int maxLatency) {
        this.maxLatency = maxLatency;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ZookeeperRecord{" +
                "hostname='" + hostname + '\'' +
                ", connections=" + connections +
                ", watches=" + watches +
                ", sent=" + sent +
                ", received=" + received +
                ", mode='" + mode + '\'' +
                ", minLatency=" + minLatency +
                ", avgLatency=" + avgLatency +
                ", maxLatency=" + maxLatency +
                ", nodeCount=" + nodeCount +
                ", date=" + date +
                ", state=" + state +
                '}';
    }
}

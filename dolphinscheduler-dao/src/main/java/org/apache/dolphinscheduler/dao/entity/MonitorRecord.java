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

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.Flag;

import java.util.Date;

/**
 * monitor record for database
 */
public class MonitorRecord {

    private DbType dbType;

    /**
     * is normal or not , 1: normal
     */
    private Flag state;

    /**
     * max connections
     */
    private long maxConnections;

    /**
     * max used connections
     */
    private long maxUsedConnections;

    /**
     * threads connections
     */
    private long threadsConnections;

    /**
     * threads running connections
     */
    private long threadsRunningConnections;

    /**
     * start date
     */
    private Date date;

    public Flag getState() {
        return state;
    }

    public void setState(Flag state) {
        this.state = state;
    }

    public long getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(long maxConnections) {
        this.maxConnections = maxConnections;
    }

    public long getMaxUsedConnections() {
        return maxUsedConnections;
    }

    public void setMaxUsedConnections(long maxUsedConnections) {
        this.maxUsedConnections = maxUsedConnections;
    }

    public long getThreadsConnections() {
        return threadsConnections;
    }

    public void setThreadsConnections(long threadsConnections) {
        this.threadsConnections = threadsConnections;
    }

    public long getThreadsRunningConnections() {
        return threadsRunningConnections;
    }

    public void setThreadsRunningConnections(long threadsRunningConnections) {
        this.threadsRunningConnections = threadsRunningConnections;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "MonitorRecord{" +
                "state=" + state +
                ", dbType=" + dbType +
                ", maxConnections=" + maxConnections +
                ", maxUsedConnections=" + maxUsedConnections +
                ", threadsConnections=" + threadsConnections +
                ", threadsRunningConnections=" + threadsRunningConnections +
                ", date=" + date +
                '}';
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }
}

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

import org.apache.dolphinscheduler.common.enums.CycleEnum;

import java.util.Date;

/**
 * cycle dependency
 */
public class CycleDependency {
    /**
     * process define id
     */
    private int processDefineId;
    /**
     * last schedule time
     */
    private Date lastScheduleTime;
    /**
     * expiration time
     */
    private Date expirationTime;
    /**
     * cycle enum
     */
    private CycleEnum cycleEnum;


    public CycleDependency(int processDefineId, Date lastScheduleTime, Date expirationTime, CycleEnum cycleEnum) {
        this.processDefineId = processDefineId;
        this.lastScheduleTime = lastScheduleTime;
        this.expirationTime = expirationTime;
        this.cycleEnum = cycleEnum;
    }

    public int getProcessDefineId() {
        return processDefineId;
    }

    public void setProcessDefineId(int processDefineId) {
        this.processDefineId = processDefineId;
    }

    public Date getLastScheduleTime() {
        return lastScheduleTime;
    }

    public void setLastScheduleTime(Date lastScheduleTime) {
        this.lastScheduleTime = lastScheduleTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public CycleEnum getCycleEnum() {
        return cycleEnum;
    }

    public void setCycleEnum(CycleEnum cycleEnum) {
        this.cycleEnum = cycleEnum;
    }

    @Override
    public String toString() {
        return "CycleDependency{" +
                "processDefineId=" + processDefineId +
                ", lastScheduleTime=" + lastScheduleTime +
                ", expirationTime=" + expirationTime +
                ", cycleEnum=" + cycleEnum +
                '}';
    }
}

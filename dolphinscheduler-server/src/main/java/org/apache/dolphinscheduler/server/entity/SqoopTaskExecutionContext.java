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

package org.apache.dolphinscheduler.server.entity;

import java.io.Serializable;

/**
 *  master/worker task transport
 */
public class SqoopTaskExecutionContext implements Serializable{

    /**
     * dataSourceId
     */
    private int dataSourceId;

    /**
     * sourcetype
     */
    private int sourcetype;

    /**
     * sourceConnectionParams
     */
    private String sourceConnectionParams;

    /**
     * dataTargetId
     */
    private int dataTargetId;

    /**
     * targetType
     */
    private int targetType;

    /**
     * targetConnectionParams
     */
    private String targetConnectionParams;

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public int getSourcetype() {
        return sourcetype;
    }

    public void setSourcetype(int sourcetype) {
        this.sourcetype = sourcetype;
    }

    public String getSourceConnectionParams() {
        return sourceConnectionParams;
    }

    public void setSourceConnectionParams(String sourceConnectionParams) {
        this.sourceConnectionParams = sourceConnectionParams;
    }

    public int getDataTargetId() {
        return dataTargetId;
    }

    public void setDataTargetId(int dataTargetId) {
        this.dataTargetId = dataTargetId;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public String getTargetConnectionParams() {
        return targetConnectionParams;
    }

    public void setTargetConnectionParams(String targetConnectionParams) {
        this.targetConnectionParams = targetConnectionParams;
    }

    @Override
    public String toString() {
        return "SqoopTaskExecutionContext{" +
                "dataSourceId=" + dataSourceId +
                ", sourcetype=" + sourcetype +
                ", sourceConnectionParams='" + sourceConnectionParams + '\'' +
                ", dataTargetId=" + dataTargetId +
                ", targetType=" + targetType +
                ", targetConnectionParams='" + targetConnectionParams + '\'' +
                '}';
    }
}

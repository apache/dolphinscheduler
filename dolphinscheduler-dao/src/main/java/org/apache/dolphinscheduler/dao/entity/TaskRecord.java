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
 * task record for qianfan
 */
public class TaskRecord {

    /**
     * id
     */
    private int id;

    /**
     * process id
     */
    private int procId;

    /**
     * procedure name
     */
    private String procName;

    /**
     * procedure date
     */
    private String procDate;

    /**
     * start date
     */
    private Date startTime;

    /**
     * end date
     */
    private Date endTime;

    /**
     * result
     */
    private String result;

    /**
     * duration unit: second
     */
    private int duration;

    /**
     * note
     */
    private String note;

    /**
     * schema
     */
    private String schema;

    /**
     * job id
     */
    private String jobId;


    /**
     * source tab
     */
    private String sourceTab;

    /**
     * source row count
     */
    private Long sourceRowCount;

    /**
     * target tab
     */
    private String targetTab;

    /**
     * target row count
     */
    private Long targetRowCount;

    /**
     * error code
     */
    private String errorCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcId() {
        return procId;
    }

    public void setProcId(int procId) {
        this.procId = procId;
    }

    public String getProcName() {
        return procName;
    }

    public void setProcName(String procName) {
        this.procName = procName;
    }

    public String getProcDate() {
        return procDate;
    }

    public void setProcDate(String procDate) {
        this.procDate = procDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSourceTab() {
        return sourceTab;
    }

    public void setSourceTab(String sourceTab) {
        this.sourceTab = sourceTab;
    }

    public Long getSourceRowCount() {
        return sourceRowCount;
    }

    public void setSourceRowCount(Long sourceRowCount) {
        this.sourceRowCount = sourceRowCount;
    }

    public String getTargetTab() {
        return targetTab;
    }

    public void setTargetTab(String targetTab) {
        this.targetTab = targetTab;
    }

    public Long getTargetRowCount() {
        return targetRowCount;
    }

    public void setTargetRowCount(Long targetRowCount) {
        this.targetRowCount = targetRowCount;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString(){
        return "task record, id:" + id
                +" proc id:" + procId
                + " proc name:" + procName
                + " proc date: " + procDate
                + " start date:" + startTime
                + " end date:" + endTime
                + " result : " + result
                + " duration : " + duration
                + " note : " + note
                + " schema : " + schema
                + " job id : " + jobId
                + " source table : " + sourceTab
                + " source row count: " + sourceRowCount
                + " target table : " + targetTab
                + " target row count: " + targetRowCount
                + " error code: " + errorCode
                ;
    }

}

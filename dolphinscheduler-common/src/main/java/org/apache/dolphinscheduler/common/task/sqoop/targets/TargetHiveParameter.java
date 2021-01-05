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
package org.apache.dolphinscheduler.common.task.sqoop.targets;

/**
 * target hive parameter
 */
public class TargetHiveParameter {

    /**
     * hive database
     */
    private String hiveDatabase;
    /**
     * hive table
     */
    private String hiveTable;
    /**
     * create hive table
     */
    private boolean createHiveTable;
    /**
     * drop delimiter
     */
    private boolean dropDelimiter;
    /**
     * hive overwrite
     */
    private boolean hiveOverWrite;

    /**
     * hive target dir
     */
    private String hiveTargetDir;
    /**
     * replace delimiter
     */
    private String replaceDelimiter;
    /**
     * hive partition key
     */
    private String hivePartitionKey;
    /**
     * hive partition value
     */
    private String hivePartitionValue;

    public String getHiveDatabase() {
        return hiveDatabase;
    }

    public void setHiveDatabase(String hiveDatabase) {
        this.hiveDatabase = hiveDatabase;
    }

    public String getHiveTable() {
        return hiveTable;
    }

    public void setHiveTable(String hiveTable) {
        this.hiveTable = hiveTable;
    }

    public boolean isCreateHiveTable() {
        return createHiveTable;
    }

    public void setCreateHiveTable(boolean createHiveTable) {
        this.createHiveTable = createHiveTable;
    }

    public boolean isDropDelimiter() {
        return dropDelimiter;
    }

    public void setDropDelimiter(boolean dropDelimiter) {
        this.dropDelimiter = dropDelimiter;
    }

    public boolean isHiveOverWrite() {
        return hiveOverWrite;
    }

    public void setHiveOverWrite(boolean hiveOverWrite) {
        this.hiveOverWrite = hiveOverWrite;
    }

    public String getReplaceDelimiter() {
        return replaceDelimiter;
    }

    public void setReplaceDelimiter(String replaceDelimiter) {
        this.replaceDelimiter = replaceDelimiter;
    }

    public String getHivePartitionKey() {
        return hivePartitionKey;
    }

    public void setHivePartitionKey(String hivePartitionKey) {
        this.hivePartitionKey = hivePartitionKey;
    }

    public String getHivePartitionValue() {
        return hivePartitionValue;
    }

    public void setHivePartitionValue(String hivePartitionValue) {
        this.hivePartitionValue = hivePartitionValue;
    }

    public String getHiveTargetDir() {
        return hiveTargetDir;
    }

    public void setHiveTargetDir(String hiveTargetDir) {
        this.hiveTargetDir = hiveTargetDir;
    }
}

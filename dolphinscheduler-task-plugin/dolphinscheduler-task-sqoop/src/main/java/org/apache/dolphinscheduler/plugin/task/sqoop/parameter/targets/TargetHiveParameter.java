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

package org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets;

import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.TargetCommonParameter;

import lombok.Data;

/**
 * target hive parameter
 */
@Data
public class TargetHiveParameter extends TargetCommonParameter {

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
    /**
     * hive target dir
     */
    private String hiveTargetDir;

}

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

package org.apache.dolphinscheduler.plugin.task.api;

import java.io.Serializable;

import lombok.Data;

/**
 * DataQualityTaskExecutionContext
 */
@Data
public class DataQualityTaskExecutionContext implements Serializable {

    /**
     * rule id
     */
    private int ruleId;
    /**
     * rule name
     */
    private String ruleName;
    /**
     * rule type
     */
    private int ruleType;
    /**
     * input entry list
     */
    private String ruleInputEntryList;
    /**
     *  execute sql list
     */
    private String executeSqlList;
    /**
     * if comparison value calculate from statistics value table
     */
    private boolean comparisonNeedStatisticsValueTable = false;
    /**
     * compare with fixed value
     */
    private boolean compareWithFixedValue = false;
    /**
     * error output path
     */
    private String hdfsPath;
    /**
     * sourceConnector type
     */
    private String sourceConnectorType;
    /**
     * source type
     */
    private int sourceType;
    /**
     * source connection params
     */
    private String sourceConnectionParams;
    /**
     * target connector type
     */
    private String targetConnectorType;
    /**
     * target type
     */
    private int targetType;
    /**
     * target connection params
     */
    private String targetConnectionParams;
    /**
     * source connector type
     */
    private String writerConnectorType;
    /**
     * writer type
     */
    private int writerType;
    /**
     * writer table
     */
    private String writerTable;
    /**
     * writer connection params
     */
    private String writerConnectionParams;
    /**
     * statistics value connector type
     */
    private String statisticsValueConnectorType;
    /**
     * statistics value type
     */
    private int statisticsValueType;
    /**
     * statistics value table
     */
    private String statisticsValueTable;
    /**
     * statistics value writer connection params
     */
    private String statisticsValueWriterConnectionParams;
}

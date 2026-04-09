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

package org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SourceCommonParameter;

import java.util.List;

import lombok.Data;

/**
 * source oracle parameter
 */
@Data
public class SourceOracleParameter extends SourceCommonParameter {

    /**
     * src table
     */
    private String srcTable;
    /**
     * src query type
     */
    private int srcQueryType;
    /**
     * src query sql
     */
    private String srcQuerySql;
    /**
     * src column type
     */
    private int srcColumnType;
    /**
     * src columns
     */
    private String srcColumns;
    /**
     * src condition list
     */
    private List<Property> srcConditionList;
    /**
     * map column hive
     */
    private List<Property> mapColumnHive;
    /**
     * map column java
     */
    private List<Property> mapColumnJava;

}

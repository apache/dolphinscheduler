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

package org.apache.dolphinscheduler.data.quality.configuration;

import org.apache.dolphinscheduler.data.quality.utils.Preconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ExecutorParameter
 */
public class ExecutorParameter implements IParameter {

    @JsonProperty("index")
    private String index;

    @JsonProperty("execute.sql")
    private String executeSql;

    @JsonProperty("table.alias")
    private String tableAlias;

    public ExecutorParameter() {
    }

    public ExecutorParameter(String index, String executeSql, String tableAlias) {
        this.index = index;
        this.executeSql = executeSql;
        this.tableAlias = tableAlias;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getExecuteSql() {
        return executeSql;
    }

    public void setExecuteSql(String executeSql) {
        this.executeSql = executeSql;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    @Override
    public void validate() {
        Preconditions.checkArgument(index != null, "index should not be empty");
        Preconditions.checkArgument(executeSql != null, "executeSql should not be empty");
        Preconditions.checkArgument(tableAlias != null, "tableAlias should not be empty");
    }
}

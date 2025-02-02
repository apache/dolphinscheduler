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

package org.apache.dolphinscheduler.plugin.task.seatunnel.parameter;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.DbTypeEnum;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "dbType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HdfsFileParameters.class, name = "HDFS"),
        @JsonSubTypes.Type(value = DorisParameters.class, name = "DORIS"),
        @JsonSubTypes.Type(value = MysqlParameters.class, name = "MYSQL")
})
public class SeatunnelConfigParameters {

    /**
     * source database type
     */
    protected DbTypeEnum dbType;

    protected List<Property> customParams;

    public DbTypeEnum getDbType() {
        return dbType;
    }

    public void setDbType(DbTypeEnum dbType) {
        this.dbType = dbType;
    }

    public List<Property> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(List<Property> customParams) {
        this.customParams = customParams;
    }
}

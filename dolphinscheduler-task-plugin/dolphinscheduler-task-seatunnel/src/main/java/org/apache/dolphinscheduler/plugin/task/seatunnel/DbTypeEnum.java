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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.seatunnel.generator.ConfigTemplate;
import org.apache.dolphinscheduler.plugin.task.seatunnel.generator.DorisConfigTemplate;
import org.apache.dolphinscheduler.plugin.task.seatunnel.generator.HdfsFileConfigTemplate;
import org.apache.dolphinscheduler.plugin.task.seatunnel.generator.MysqlConfigTemplate;

public enum DbTypeEnum {

    HDFS("hdfs"),
    HIVE("hive"),
    DORIS("doris"),
    MYSQL("mysql"),
    CLICKHOUSE("clickhouse");

    private final String name;

    DbTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConfigTemplate toConfigTemplate(SeatunnelParameters seatunnelConfigParameters,
                                           SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {
        switch (this) {
            case HDFS:
                return new HdfsFileConfigTemplate(seatunnelConfigParameters, seatunnelTaskExecutionContext);
            case DORIS:
                return new DorisConfigTemplate(seatunnelConfigParameters, seatunnelTaskExecutionContext);
            case MYSQL:
                return new MysqlConfigTemplate(seatunnelConfigParameters, seatunnelTaskExecutionContext);
            default:
                throw new IllegalArgumentException("not support db type => " + name);
        }
    }

    public boolean isDatasourceType() {
        return this != HDFS;
    }
}

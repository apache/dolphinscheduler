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

package org.apache.dolphinscheduler.plugin.task.spark;

public enum SparkCommand {

    /**
     * 0 SPARK1SUBMIT
     * 1 SPARK2SUBMIT
     * 2 SPARK1SQL
     * 3 SPARK2SQL
     */
    SPARK1SUBMIT(0, "SPARK1SUBMIT", "${SPARK_HOME1}/bin/spark-submit", SparkVersion.SPARK1),
    SPARK2SUBMIT(1, "SPARK2SUBMIT", "${SPARK_HOME2}/bin/spark-submit", SparkVersion.SPARK2),

    SPARK1SQL(2, "SPARK1SQL", "${SPARK_HOME1}/bin/spark-sql", SparkVersion.SPARK1),

    SPARK2SQL(3, "SPARK2SQL", "${SPARK_HOME2}/bin/spark-sql", SparkVersion.SPARK2);

    private final int code;
    private final String descp;
    /**
     * usage: spark-submit [options] <app jar | python file> [app arguments]
     */
    private final String command;
    private final SparkVersion sparkVersion;

    SparkCommand(int code, String descp, String command, SparkVersion sparkVersion) {
        this.code = code;
        this.descp = descp;
        this.command = command;
        this.sparkVersion = sparkVersion;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    public String getCommand() {
        return command;
    }

    public SparkVersion getSparkVersion() {
        return sparkVersion;
    }
}

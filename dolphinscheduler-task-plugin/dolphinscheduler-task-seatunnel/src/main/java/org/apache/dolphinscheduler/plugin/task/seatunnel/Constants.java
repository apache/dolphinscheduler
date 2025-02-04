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

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String CONFIG_OPTIONS = "--config";
    public static final String DEPLOY_MODE_OPTIONS = "--deploy-mode";
    public static final String MASTER_OPTIONS = "--master";
    public static final String STARTUP_SCRIPT_SPARK = "spark";
    public static final String STARTUP_SCRIPT_FLINK = "flink";
    public static final String STARTUP_SCRIPT_SEATUNNEL = "seatunnel";
    public static final String JSON_SUFFIX = "json";
    public static final String CONF_SUFFIX = "conf";

    public static final String EQUAL_SIGN = " = ";
    public static final String LINE_BREAK = "\n";
    public static final String INDENT_TWO_SPACE = "  ";
    public static final String INDENT_FOUR_SPACE = "    ";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String SINGLE_BRACKETS_RIGHT = "}";
    public static final String SINGLE_BRACKETS_LEFT = "{";

    /**
     * doris config
     */
    public static final String DORIS_CONFIG = "doris.config";
    public static final String FORMAT_JSON = "json";
    public static final String READ_JSON_BY_LINE_VALUE = "true";
    public static final String DORIS_CONFIG_SINK_TEMPLATE = "    doris.config {\n" +
            "      format = \"%s\"" + "\n" +
            "      read_json_by_line = \"%s\"" + "\n" +
            "    }\n";
    public static final String DORIS_HTTP_PORT = "8030";

    /**
     * mysql config
     */
    public static final String MYSQL_EXTRA_SINK_PARAMS =
            "    database = \"%s\"\n    table = \"%s\"\n    generate_sink_sql = \"%s\"\n";
    public static final String MYSQL_QUERY_PARAMS = "    query = \"%s\"\n";
    public static final String MYSQL_DEFAULT_QUERY_PREFIX = "select * from ";

    public static final String MYSQL = "MYSQL";
    public static final String HDFS = "HDFS";
    public static final String DORIS = "DORIS";
}

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

package org.apache.dolphinscheduler.plugin.task.api.utils;

/**
 * DataQualityConstants
 */
public class DataQualityConstants {

    private DataQualityConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * data quality task
     */
    public static final String SRC_CONNECTOR_TYPE = "src_connector_type";
    public static final String SRC_DATASOURCE_ID = "src_datasource_id";
    public static final String SRC_DATABASE = "src_database";
    public static final String SRC_TABLE = "src_table";
    public static final String SRC_FILTER = "src_filter";
    public static final String SRC_FIELD = "src_field";
    public static final String TARGET_CONNECTOR_TYPE = "target_connector_type";
    public static final String TARGET_DATASOURCE_ID = "target_datasource_id";
    public static final String TARGET_DATABASE = "target_database";
    public static final String TARGET_TABLE = "target_table";
    public static final String TARGET_FILTER = "target_filter";
    public static final String TARGET_FIELD = "target_field";
    public static final String STATISTICS_NAME = "statistics_name";
    public static final String STATISTICS_EXECUTE_SQL = "statistics_execute_sql";
    public static final String COMPARISON_NAME = "comparison_name";
    public static final String COMPARISON_TYPE = "comparison_type";
    public static final String COMPARISON_VALUE = "comparison_value";
    public static final String COMPARISON_EXECUTE_SQL = "comparison_execute_sql";
    public static final String MAPPING_COLUMNS = "mapping_columns";
    public static final String ON_CLAUSE = "on_clause";
    public static final String WHERE_CLAUSE = "where_clause";
    public static final String CHECK_TYPE = "check_type";
    public static final String THRESHOLD = "threshold";
    public static final String OPERATOR = "operator";
    public static final String FAILURE_STRATEGY = "failure_strategy";
    public static final String STATISTICS_TABLE = "statistics_table";
    public static final String COMPARISON_TABLE = "comparison_table";
    public static final String AND = " AND ";
    public static final String WRITER_CONNECTOR_TYPE = "writer_connector_type";
    public static final String WRITER_DATASOURCE_ID = "writer_datasource_id";
    public static final String UNIQUE_CODE = "unique_code";
    public static final String DATA_TIME = "data_time";
    public static final String REGEXP_PATTERN = "regexp_pattern";
    public static final String ERROR_OUTPUT_PATH = "error_output_path";
    public static final String INDEX = "index";
    public static final String PATH = "path";
    public static final String HDFS_FILE = "hdfs_file";
    public static final String BATCH = "batch";

    public static final String RULE_ID = "rule_id";
    public static final String RULE_TYPE = "rule_type";
    public static final String RULE_NAME = "rule_name";
    public static final String CREATE_TIME = "create_time";
    public static final String UPDATE_TIME = "update_time";
    public static final String PROCESS_DEFINITION_ID = "process_definition_id";
    public static final String PROCESS_INSTANCE_ID = "process_instance_id";
    public static final String TASK_INSTANCE_ID = "task_instance_id";

    public static final String ADDRESS = "address";
    public static final String DATABASE = "database";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String PRINCIPAL = "principal";
    public static final String OTHER = "other";
    public static final String ORACLE_DB_CONNECT_TYPE = "connectType";

    public static final String TABLE = "table";
    public static final String URL = "url";
    public static final String DRIVER = "driver";
    public static final String SQL = "sql";
    public static final String INPUT_TABLE = "input_table";
    public static final String OUTPUT_TABLE = "output_table";
    public static final String TMP_TABLE = "tmp_table";

    public static final String USER = "user";
    public static final String PASSWORD = "password";

    /**
     * database type
     */
    public static final String MYSQL = "MYSQL";
    public static final String POSTGRESQL = "POSTGRESQL";

}

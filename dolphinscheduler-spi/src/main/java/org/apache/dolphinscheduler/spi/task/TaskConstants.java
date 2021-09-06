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

package org.apache.dolphinscheduler.spi.task;

public class TaskConstants {

    private TaskConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String APPLICATION_REGEX = "application_\\d+_\\d+";

    /**
     * string false
     */
    public static final String STRING_FALSE = "false";

    /**
     * exit code kill
     */
    public static final int EXIT_CODE_KILL = 137;
    public static final String PID = "pid";

    /**
     * comma ,
     */
    public static final String COMMA = ",";

    /**
     * sleep time
     */
    public static final int SLEEP_TIME_MILLIS = 1000;

    /**
     * exit code failure
     */
    public static final int EXIT_CODE_FAILURE = -1;

    /**
     * exit code success
     */
    public static final int EXIT_CODE_SUCCESS = 0;

    public static final String SH = "sh";

    /**
     * default log cache rows num,output when reach the number
     */
    public static final int DEFAULT_LOG_ROWS_NUM = 4 * 16;

    /**
     * log flush interval?output when reach the interval
     */
    public static final int DEFAULT_LOG_FLUSH_INTERVAL = 1000;

    /**
     * pstree, get pud and sub pid
     */
    public static final String PSTREE = "pstree";

    public static final String RWXR_XR_X = "rwxr-xr-x";

    /**
     * task log info format
     */
    public static final String TASK_LOG_INFO_FORMAT = "TaskLogInfo-%s";

    /**
     * date format of yyyyMMdd
     */
    public static final String PARAMETER_FORMAT_DATE = "yyyyMMdd";

    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String PARAMETER_FORMAT_TIME = "yyyyMMddHHmmss";

    /**
     * new
     * schedule time
     */
    public static final String PARAMETER_SHECDULE_TIME = "schedule.time";

    /**
     * system date(yyyyMMddHHmmss)
     */
    public static final String PARAMETER_DATETIME = "system.datetime";

    /**
     * system date(yyyymmdd) today
     */
    public static final String PARAMETER_CURRENT_DATE = "system.biz.curdate";

    /**
     * system date(yyyymmdd) yesterday
     */
    public static final String PARAMETER_BUSINESS_DATE = "system.biz.date";

    /**
     * the absolute path of current executing task
     */
    public static final String PARAMETER_TASK_EXECUTE_PATH = "system.task.execute.path";

    /**
     * the instance id of current task
     */
    public static final String PARAMETER_TASK_INSTANCE_ID = "system.task.instance.id";

    /**
     * month_begin
     */
    public static final String MONTH_BEGIN = "month_begin";
    /**
     * add_months
     */
    public static final String ADD_MONTHS = "add_months";
    /**
     * month_end
     */
    public static final String MONTH_END = "month_end";
    /**
     * week_begin
     */
    public static final String WEEK_BEGIN = "week_begin";
    /**
     * week_end
     */
    public static final String WEEK_END = "week_end";
    /**
     * timestamp
     */
    public static final String TIMESTAMP = "timestamp";
    public static final char SUBTRACT_CHAR = '-';
    public static final char ADD_CHAR = '+';
    public static final char MULTIPLY_CHAR = '*';
    public static final char DIVISION_CHAR = '/';
    public static final char LEFT_BRACE_CHAR = '(';
    public static final char RIGHT_BRACE_CHAR = ')';
    public static final String ADD_STRING = "+";
    public static final String MULTIPLY_STRING = "*";
    public static final String DIVISION_STRING = "/";
    public static final String LEFT_BRACE_STRING = "(";
    public static final char P = 'P';
    public static final char N = 'N';
    public static final String SUBTRACT_STRING = "-";
    public static final String GLOBAL_PARAMS = "globalParams";
    public static final String LOCAL_PARAMS = "localParams";
    public static final String LOCAL_PARAMS_LIST = "localParamsList";
    public static final String SUBPROCESS_INSTANCE_ID = "subProcessInstanceId";
    public static final String PROCESS_INSTANCE_STATE = "processInstanceState";
    public static final String PARENT_WORKFLOW_INSTANCE = "parentWorkflowInstance";
    public static final String CONDITION_RESULT = "conditionResult";
    public static final String SWITCH_RESULT = "switchResult";
    public static final String DEPENDENCE = "dependence";
    public static final String TASK_TYPE = "taskType";
    public static final String TASK_LIST = "taskList";
    public static final String QUEUE = "queue";
    public static final String QUEUE_NAME = "queueName";
    public static final int LOG_QUERY_SKIP_LINE_NUMBER = 0;
    public static final int LOG_QUERY_LIMIT = 4096;


}

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

package org.apache.dolphinscheduler.plugin.task.datavines;

/**
 * Custom DinkyTaskConstants
 */
public class DatavinesTaskConstants {

    private DatavinesTaskConstants() {
        throw new IllegalStateException("Utility class");
    }

    private static final String API_ROUTE = "/api/v1/openapi";
    public static final String EXECUTE_JOB = API_ROUTE + "/job/execute/";
    public static final String GET_JOB_EXECUTION_STATUS = API_ROUTE + "/job/execution/status/";
    public static final String GET_JOB_EXECUTION_RESULT = API_ROUTE + "/job/execution/result/";
    public static final String JOB_EXECUTION_KILL = API_ROUTE + "/job/execution/kill/";
    public static final String API_RESULT_CODE = "code";
    public static final int API_RESULT_CODE_SUCCESS = 200;
    public static final String API_RESULT_MSG = "msg";
    public static final String API_RESULT_DATA = "data";
    public static final String API_ERROR_MSG = "please check url or params";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_KILL = "KILL";
    public static final String STATUS_FAILURE = "FAILURE";

    public static final String SUBMIT_FAILED_MSG = "Submit datavinesTask failed:";
    public static final String TRACK_FAILED_MSG = "Track datavinesTask failed:";
    public static final String APPIDS_FORMAT = "%s-%s";

    public static final long SLEEP_MILLIS = 3000;

}

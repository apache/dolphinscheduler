package org.apache.dolphinscheduler.plugin.task.api;/*
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

public class TaskConstants {

    /**
     * http connect time out
     */
    public static final int HTTP_CONNECT_TIMEOUT = 60 * 1000;


    /**
     * http connect request time out
     */
    public static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 60 * 1000;

    /**
     * httpclient soceket time out
     */
    public static final int SOCKET_TIMEOUT = 60 * 1000;


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

    //todo
    public static final String SH="sh";

    /**
     * FAILED
     */
    public static final String FAILED = "FAILED";

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


    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * kerberos expire time
     */
    public static final String KERBEROS_EXPIRE_TIME = "kerberos.expire.time";

    /**
     * hdfs/s3 configuration
     * resource.upload.path
     */
    public static final String RESOURCE_UPLOAD_PATH = "resource.upload.path";

    //todo
    public static final String SYSTEM_ENV_PATH = "";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE = "hadoop.security.authentication.startup.state";


    /**
     * loginUserFromKeytab user
     */
    public static final String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";

    /**
     * loginUserFromKeytab path
     */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    /**
     * java.security.krb5.conf.path
     */
    public static final String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";




}

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

package org.apache.dolphinscheduler.api.test.core.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {

    /**
     * backend api url
     */
    public static final String DOLPHINSCHEDULER_API_URL = "http://0.0.0.0:12345/dolphinscheduler";

    public static final String DOLPHINSCHEDULER_BASE_URL = "http://0.0.0.0";

    public static final String DOLPHINSCHEDULER_BASE_PATH = "/dolphinscheduler";

    public static final int DOLPHINSCHEDULER_API_PORT = 12345;

    public static final String USER_NAME = "admin";
    public static final String USER_PASSWD = "dolphinscheduler123";

    public static final String USER_EMAIL = "admin@dolphinscheduler.com";

    /**
     * backend api request header's content type
     */
    public static final String REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    /**
     * header's session id's key
     */
    public static final String SESSION_ID_KEY = "sessionId";

    /**
     * simple date format
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * date format of yyyyMMddHHmmssSSS
     */
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";


    /**
     * docker compose default healthy timeout
     */
    public static final Integer DOCKER_COMPOSE_DEFAULT_TIMEOUT = 180;

    public static final String QUESTION_MARK = "?";

    public static final String EQUAL_MARK = "=";

    public static final String AND_MARK = "&";

    public static final String CODE_KEY = "code";

    public static final String MSG_KEY = "msg";

    public static final String DATA_KEY = "data";

    public static final String SUCCESS_KEY = "success";

    public static final String FAILED_KEY = "failed";

    public static final String USER_NAME_KEY = "userName";

    public static final String USER_PASSWD_KEY = "userPassword";

    public static final String TENANT_CODE_KEY = "tenantCode";

}

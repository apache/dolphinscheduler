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

package org.apache.dolphinscheduler.plugin.alert.http;

public final class HttpAlertConstants {

    public static final String URL = "$t('url')";

    public static final String NAME_URL = "url";

    public static final String HEADER_PARAMS = "$t('headerParams')";

    public static final String NAME_HEADER_PARAMS = "headerParams";

    public static final String BODY_PARAMS = "$t('bodyParams')";

    public static final String NAME_BODY_PARAMS = "bodyParams";

    public static final String CONTENT_FIELD = "$t('contentField')";

    public static final String NAME_CONTENT_FIELD = "contentField";

    public static final String REQUEST_TYPE = "$t('requestType')";

    public static final String NAME_REQUEST_TYPE = "requestType";

    private HttpAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

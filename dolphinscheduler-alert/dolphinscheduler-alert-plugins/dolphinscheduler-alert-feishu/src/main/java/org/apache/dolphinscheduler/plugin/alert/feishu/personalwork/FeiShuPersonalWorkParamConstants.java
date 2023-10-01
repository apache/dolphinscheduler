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

package org.apache.dolphinscheduler.plugin.alert.feishu.personalwork;

public final class FeiShuPersonalWorkParamConstants {

    static final String PERSONAL_WORK_APP_ID = "$t('appId')";
    static final String NAME_PERSONAL_WORK_APP_ID = "appId";
    static final String PERSONAL_WORK_APP_SECRET = "$t('appSecret')";
    static final String NAME_PERSONAL_WORK_APP_SECRET = "appSecret";
    static final String OPEN_ID = "open_id";
    static final String USER_ID = "user_id";
    static final String UNION_ID = "union_id";
    static final String EMAIL = "email";
    static final String CHAT_ID = "chat_id";
    static final String RECEIVE_ID = "$t('receiveId')";
    static final String NAME_RECEIVE_ID = "receiveId";
    static final String RECEIVE_ID_TYPE = "$t('receiveIdType')";
    static final String NAME_RECEIVE_ID_TYPE = "receiveIdType";

    private FeiShuPersonalWorkParamConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

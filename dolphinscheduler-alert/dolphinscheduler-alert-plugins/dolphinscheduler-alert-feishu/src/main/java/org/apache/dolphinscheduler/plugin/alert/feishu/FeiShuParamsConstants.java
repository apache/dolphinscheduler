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

package org.apache.dolphinscheduler.plugin.alert.feishu;

public final class FeiShuParamsConstants {

    public static final String FEI_SHU_PROXY_ENABLE = "$t('isEnableProxy')";
    static final String WEB_HOOK = "$t('webhook')";
    static final String NAME_WEB_HOOK = "WebHook";
    static final String NAME_FEI_SHU_PROXY_ENABLE = "IsEnableProxy";

    static final String FEI_SHU_PROXY = "$t('proxy')";

    static final String NAME_FEI_SHU_PROXY = "Proxy";

    static final String FEI_SHU_PORT = "$t('port')";

    static final String NAME_FEI_SHU_PORT = "Port";

    static final String FEI_SHU_USER = "$t('user')";

    static final String NAME_FEI_SHU_USER = "User";

    static final String FEI_SHU_PASSWORD = "$t('password')";

    static final String NAME_FEI_SHU_PASSWORD = "Password";

    private FeiShuParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

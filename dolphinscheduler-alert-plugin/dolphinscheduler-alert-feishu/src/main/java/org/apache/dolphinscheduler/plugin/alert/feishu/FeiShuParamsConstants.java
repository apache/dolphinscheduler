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

public class FeiShuParamsConstants {

    private FeiShuParamsConstants() {
        throw new IllegalStateException("Utility class");
    }

    static final String FEI_SHU_WEB_HOOK = "feishu.webhook";

    static final String NAME_FEI_SHU_WEB_HOOK = "feiShuWebHook";

    public static final String FEI_SHU_PROXY_ENABLE = "feishu.isEnableProxy";

    static final String NAME_FEI_SHU_PROXY_ENABLE = "feiShuIsEnableProxy";

    static final String FEI_SHU_PROXY = "feishu.proxy";

    static final String NAME_FEI_SHU_PROXY = "feiShuProxy";

    static final String FEI_SHU_PORT = "feishu.port";

    static final String NAME_FEI_SHU_PORT = "feiShuPort";

    static final String FEI_SHU_USER = "feishu.user";

    static final String NAME_FEI_SHU_USER = "feiShuUser";

    static final String FEI_SHU_PASSWORD = "feishu.password";

    static final String NAME_FEI_SHU_PASSWORD = "feiShuPassword";
}

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

package org.apache.dolphinscheduler.api.enums.v2;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

public enum TenantStatus implements Status {

    TENANT_NOT_EXIST(10017, "tenant [{0}] not exists", "租户[{0}]不存在"),
    CURRENT_LOGIN_USER_TENANT_NOT_EXIST(10181, "the tenant of the currently login user is not specified",
            "未指定当前登录用户的租户"),
    DELETE_TENANT_BY_ID_FAIL_TENANTS(10309, "delete queue by id fail, for there are {0} tenants using it",
            "删除队列失败，有[{0}]个租户正在使用"),
            ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    TenantStatus(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}

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

public enum RuleStatus implements Status {

    GET_RULE_FORM_CREATE_JSON_ERROR(1200012, "get rule form create json error", "获取规则 FROM-CREATE-JSON 错误"),
    QUERY_RULE_LIST_PAGING_ERROR(1200013, "query rule list paging error", "获取规则分页列表错误"),
    QUERY_RULE_LIST_ERROR(1200014, "query rule list error", "获取规则列表错误"),
    QUERY_RULE_INPUT_ENTRY_LIST_ERROR(1200015, "query rule list error", "获取规则列表错误"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    RuleStatus(int code, String enMsg, String zhMsg) {
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
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

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

/**
 * base status enum
 */
public enum BaseStatus {
    SUCCESS(0, "success", "成功"),
    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    QUERY_ACCESSTOKEN_BY_USER_ERROR(70016, "query access token by user error", "查询访问指定用户的token错误"),
    ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    BaseStatus(int code, String enMsg, String zhMsg) {
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

    /**
     * Retrieve Status enum entity by status code.
     */
    public static Optional<BaseStatus> findStatusBy(int code) {
        for (BaseStatus status : BaseStatus.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}

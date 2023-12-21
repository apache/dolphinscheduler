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

public enum UDFStatus implements Status {

    CREATE_UDF_FUNCTION_ERROR(10065, "create udf function error", "创建UDF函数错误"),
    VIEW_UDF_FUNCTION_ERROR(10066, "view udf function error", "查询UDF函数错误"),
    UPDATE_UDF_FUNCTION_ERROR(10067, "update udf function error", "更新UDF函数错误"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR(10068, "query udf function list paging error", "分页查询UDF函数列表错误"),
    UDF_FUNCTION_NOT_EXIST(20001, "UDF function not found", "UDF函数不存在"),
    UDF_FUNCTION_EXISTS(20002, "UDF function already exists", "UDF函数已存在"),
    UDF_RESOURCE_SUFFIX_NOT_JAR(20009, "UDF resource suffix name must be jar", "UDF资源文件后缀名只支持[jar]"),
    UDF_RESOURCE_IS_BOUND(20013, "udf resource file is bound by UDF functions:{0}", "udf函数绑定了资源文件[{0}]"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    UDFStatus(int code, String enMsg, String zhMsg) {
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

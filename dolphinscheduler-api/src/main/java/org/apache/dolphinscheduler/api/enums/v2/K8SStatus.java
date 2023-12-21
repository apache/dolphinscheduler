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

public enum K8SStatus implements Status {

    QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR(1300001, "login user query k8s namespace list paging error",
            "分页查询k8s名称空间列表错误"),
    K8S_NAMESPACE_EXIST(1300002, "k8s namespace {0} already exists", "k8s命名空间[{0}]已存在"),
    CREATE_K8S_NAMESPACE_ERROR(1300003, "create k8s namespace error", "创建k8s命名空间错误"),
    UPDATE_K8S_NAMESPACE_ERROR(1300004, "update k8s namespace error", "更新k8s命名空间信息错误"),
    K8S_NAMESPACE_NOT_EXIST(1300005, "k8s namespace {0} not exists", "命名空间ID[{0}]不存在"),
    K8S_CLIENT_OPS_ERROR(1300006, "k8s error with exception {0}", "k8s操作报错[{0}]"),
    VERIFY_K8S_NAMESPACE_ERROR(1300007, "verify k8s and namespace error", "验证k8s命名空间信息错误"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    K8SStatus(int code, String enMsg, String zhMsg) {
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

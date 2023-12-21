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

public enum ClusterStatus implements Status {

    CREATE_CLUSTER_ERROR(120020, "create cluster error", "创建集群失败"),
    CLUSTER_NAME_EXISTS(120021, "this cluster name [{0}] already exists", "集群名称[{0}]已经存在"),
    CLUSTER_NAME_IS_NULL(120022, "this cluster name shouldn't be empty.", "集群名称不能为空"),
    CLUSTER_CONFIG_IS_NULL(120023, "this cluster config shouldn't be empty.", "集群配置信息不能为空"),
    DELETE_CLUSTER_ERROR(120025, "delete cluster error", "删除集群信息失败"),
    CLUSTER_NOT_EXISTS(120033, "this cluster can not found in db.", "集群配置数据库里查询不到为空"),
    DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS(120034, "this cluster has been used in namespace,so you can't delete it.",
            "该集群已经被命名空间使用，所以不能删除该集群信息"),

    QUERY_CLUSTER_BY_CODE_ERROR(1200028, "not found cluster [{0}] ", "查询集群编码[{0}]不存在"),
    QUERY_CLUSTER_BY_NAME_ERROR(1200027, "not found cluster [{0}] ", "查询集群名称[{0}]信息不存在"),

    ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ClusterStatus(int code, String enMsg, String zhMsg) {
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

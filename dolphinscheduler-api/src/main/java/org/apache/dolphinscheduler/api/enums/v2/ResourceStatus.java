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

public enum ResourceStatus implements Status {

    CREATE_RESOURCE_ERROR(10054, "create resource error", "创建资源错误"),
    UPDATE_RESOURCE_ERROR(10055, "update resource error", "更新资源错误"),
    QUERY_RESOURCES_LIST_ERROR(10056, "query resources list error", "查询资源列表错误"),
    QUERY_RESOURCES_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),
    DELETE_RESOURCE_ERROR(10058, "delete resource error", "删除资源错误"),
    RESOURCE_FILE_IS_EMPTY(10062, "resource file is empty", "资源文件内容不能为空"),
    RESOURCE_NOT_EXIST(20004, "resource not exist", "资源不存在"),
    RESOURCE_EXIST(20005, "resource already exists", "资源已存在"),
    RESOURCE_SUFFIX_NOT_SUPPORT_VIEW(20006, "resource suffix do not support online viewing", "资源文件后缀不支持查看"),
    RESOURCE_SIZE_EXCEED_LIMIT(20007, "upload resource file size exceeds limit", "上传资源文件大小超过限制"),
    RESOURCE_SUFFIX_FORBID_CHANGE(20008, "resource suffix not allowed to be modified", "资源文件后缀不支持修改"),
    RESOURCE_FILE_EXIST(20011, "resource file {0} already exists in hdfs,please delete it or change name!",
            "资源文件[{0}]在hdfs中已存在，请删除或修改资源名"),
    RESOURCE_FILE_NOT_EXIST(20012, "resource file {0} not exists !", "资源文件[{0}]不存在"),
    RESOURCE_FULL_NAME_TOO_LONG_ERROR(1300015, "resource's fullname is too long error", "资源文件名过长"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ResourceStatus(int code, String enMsg, String zhMsg) {
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

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

public enum BaseStatus implements Status {

    SUCCESS(0, "success", "成功"),
    INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),
    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    MASTER_NOT_EXISTS(10025, "master does not exist", "无可用master节点"),
    NAME_NULL(10134, "name must be not null", "名称不能为空"),
    SAVE_ERROR(10136, "save error", "保存错误"),
    CONNECTION_TEST_FAILURE(10037, "connection test failure", "测试数据源连接失败"),
    DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
    DATA_IS_NULL(50018, "data {0} is null", "数据[{0}]不能为空"),

    MAIN_TABLE_USING_VERSION(50053, "the version that the master table is using", "主表正在使用该版本"),
    DELETE_EDGE_ERROR(50055, "delete edge error", "删除工作流任务连接线错误"),
    S3_CANNOT_RENAME(60003, "directory cannot be renamed", "S3无法重命名文件夹"),
    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist, tokenId {0}", "访问token不存在, {0}"),
    NEGTIVE_SIZE_NUMBER_ERROR(80002, "query size number error", "查询size错误"),
    START_TIME_BIGGER_THAN_END_TIME_ERROR(80003, "start time bigger than end time error", "开始时间在结束时间之后错误"),
    NOT_ALLOW_TO_DISABLE_OWN_ACCOUNT(130020, "Not allow to disable your own account", "不能停用自己的账号"),
    TIME_ZONE_ILLEGAL(130031, "time zone [{0}] is illegal", "时区参数 [{0}] 不合法"),
    VERIFY_PARAMETER_NAME_FAILED(1300009, "The file name verify failed", "文件命名校验失败"),
    STORE_OPERATE_CREATE_ERROR(1300010, "create the resource failed", "存储操作失败"),
    FUNCTION_DISABLED(1400002, "The current feature is disabled.", "当前功能已被禁用"),
    DESCRIPTION_TOO_LONG_ERROR(1400004, "description is too long error", "描述过长"),;
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
}

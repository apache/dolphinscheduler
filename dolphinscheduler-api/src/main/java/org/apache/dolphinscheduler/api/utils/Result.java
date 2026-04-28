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

package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.api.enums.Status;

import java.text.MessageFormat;

import lombok.Data;

/**
 * result
 *
 * @param <T> T
 */
@Data
public class Result<T> {

    /**
     * status
     */
    private Integer code;

    private String msg;

    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(Status status) {
        if (status != null) {
            this.code = status.getCode();
            this.msg = status.getMsg();
        }
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * Call this function if there is success
     *
     * @param data data
     * @param <T> type
     * @return resule
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(Status.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(Status status) {
        return this.code != null && this.code.equals(status.getCode());
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @return result
     */
    public static <T> Result<T> error(Status status) {
        return new Result<>(status);
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @param args args
     * @return result
     */
    public static <T> Result<T> errorWithArgs(Status status, Object... args) {
        return new Result<>(status.getCode(), MessageFormat.format(status.getMsg(), args));
    }

    @Override
    public String toString() {
        return "Status{"
                + "code='" + code
                + '\'' + ", msg='"
                + msg + '\''
                + ", data=" + data
                + '}';
    }

}

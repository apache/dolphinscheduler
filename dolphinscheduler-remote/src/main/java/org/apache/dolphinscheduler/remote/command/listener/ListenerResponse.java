/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.remote.command.listener;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.ResponseMessageBuilder;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ListenerResponse<T> implements ResponseMessageBuilder {

    private boolean success;
    private String message;
    private T data;

    public ListenerResponse() {
    }

    public ListenerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ListenerResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ListenerResponse<T> success() {
        return new ListenerResponse<>(true, "成功", null);
    }

    public static <T> ListenerResponse<T> success(T data) {
        return new ListenerResponse<>(true, "成功", data);
    }

    public static <T> ListenerResponse<T> fail(String msg) {
        return new ListenerResponse<>(false, msg, null);
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.RESPONSE;
    }
}

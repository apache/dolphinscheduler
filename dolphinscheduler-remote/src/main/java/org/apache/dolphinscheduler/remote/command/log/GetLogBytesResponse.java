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

package org.apache.dolphinscheduler.remote.command.log;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.ResponseMessageBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetLogBytesResponse implements ResponseMessageBuilder {

    private byte[] data;

    private Status responseStatus;

    public static GetLogBytesResponse error(@NonNull Status status) {
        return GetLogBytesResponse.builder()
                .responseStatus(status)
                .build();
    }

    public static GetLogBytesResponse success(byte[] logBytes) {
        return GetLogBytesResponse.builder()
                .responseStatus(Status.SUCCESS)
                .data(logBytes)
                .build();
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.RESPONSE;
    }

    public enum Status {

        SUCCESS("success"),
        COMMAND_IS_NULL("RPC command is null"),
        LOG_PATH_IS_NOT_SECURITY("Log file path is not at a security directory"),
        LOG_FILE_NOT_FOUND("Log file doesn't exist"),
        UNKNOWN_ERROR("Meet an unknown exception"),
        ;

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

}

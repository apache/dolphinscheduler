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
package org.apache.dolphinscheduler.api.exceptions;

import org.apache.dolphinscheduler.api.enums.Status;

import java.text.MessageFormat;

import lombok.Data;

@Data
public class ServiceException extends RuntimeException {

    private int code;

    public ServiceException() {
        this(Status.INTERNAL_SERVER_ERROR_ARGS);
    }

    public ServiceException(Status status) {
        this(status.getCode(), status.getMsg());
    }

    public ServiceException(Status status, Object... formatter) {
        this(status.getCode(), MessageFormat.format(status.getMsg(), formatter));
    }

    public ServiceException(String message) {
        this(Status.INTERNAL_SERVER_ERROR_ARGS, message);
    }

    public ServiceException(int code, String message) {
        this(code, message, null);
    }

    public ServiceException(int code, String message, Exception cause) {
        super(message, cause);
        this.code = code;
    }

}

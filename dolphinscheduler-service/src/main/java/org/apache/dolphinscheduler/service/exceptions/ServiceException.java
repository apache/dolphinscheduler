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

package org.apache.dolphinscheduler.service.exceptions;

/**
 * Custom ZKServerException exception
 */
public class ServiceException extends RuntimeException {

    /**
     * Construct a new runtime exception with the error message
     *
     * @param errMsg Error message
     */
    public ServiceException(String errMsg) {
        super(errMsg);
    }

    /**
     * Construct a new runtime exception with the cause
     *
     * @param cause cause
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param errMsg message
     * @param cause cause
     */
    public ServiceException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}

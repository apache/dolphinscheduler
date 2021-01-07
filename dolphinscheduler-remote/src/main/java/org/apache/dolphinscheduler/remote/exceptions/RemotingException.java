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

package org.apache.dolphinscheduler.remote.exceptions;

/**
 *  remote exception
 */
public class RemotingException extends Exception {

    public RemotingException() {
        super();
    }

    /**
     * Construct a new runtime exception with the detail message
     *
     * @param   message  detail message
     */
    public RemotingException(String message) {
        super(message);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param  message the detail message
     * @param  cause the cause
     * @since  1.4
     */
    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new runtime exception with throwable
     *
     * @param  cause the cause
     */
    public RemotingException(Throwable cause) {
        super(cause);
    }


}

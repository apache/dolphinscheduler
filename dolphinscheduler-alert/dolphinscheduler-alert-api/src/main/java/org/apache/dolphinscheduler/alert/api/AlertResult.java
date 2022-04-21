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

package org.apache.dolphinscheduler.alert.api;

import java.util.Objects;

/**
 * alert result
 */
public class AlertResult {
    private String status;
    private String message;

    public AlertResult(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public AlertResult() {
    }

    public static AlertResultBuilder builder() {
        return new AlertResultBuilder();
    }

    public String getStatus() {
        return this.status;
    }

    public AlertResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public AlertResult setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertResult)) {
            return false;
        }
        final AlertResult other = (AlertResult) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object thisStatus = this.getStatus();
        final Object otherStatus = other.getStatus();
        if (!Objects.equals(thisStatus, otherStatus)) {
            return false;
        }
        final Object thisMessage = this.getMessage();
        final Object otherMessage = other.getMessage();
        return Objects.equals(thisMessage, otherMessage);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AlertResult;
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        final Object s = this.getStatus();
        result = result * prime + (s == null ? 43 : s.hashCode());
        final Object message = this.getMessage();
        result = result * prime + (message == null ? 43 : message.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AlertResult(status=" + this.getStatus() + ", message=" + this.getMessage() + ")";
    }

    public static class AlertResultBuilder {
        private String status;
        private String message;

        AlertResultBuilder() {
        }

        public AlertResultBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AlertResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AlertResult build() {
            return new AlertResult(status, message);
        }

        @Override
        public String toString() {
            return "AlertResult.AlertResultBuilder(status=" + this.status + ", message=" + this.message + ")";
        }
    }
}

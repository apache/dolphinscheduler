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

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertResult)) {
            return false;
        }
        final AlertResult other = (AlertResult) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AlertResult;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

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

        public String toString() {
            return "AlertResult.AlertResultBuilder(status=" + this.status + ", message=" + this.message + ")";
        }
    }
}

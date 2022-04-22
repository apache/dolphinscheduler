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

import java.util.Map;
import java.util.Objects;

/**
 * The alarm information includes the parameters of the alert channel and the alarm data
 */
public class AlertInfo {
    private Map<String, String> alertParams;
    private AlertData alertData;

    public AlertInfo(Map<String, String> alertParams, AlertData alertData) {
        this.alertParams = alertParams;
        this.alertData = alertData;
    }

    public AlertInfo() {
    }

    public static AlertInfoBuilder builder() {
        return new AlertInfoBuilder();
    }

    public Map<String, String> getAlertParams() {
        return this.alertParams;
    }

    public AlertInfo setAlertParams(Map<String, String> alertParams) {
        this.alertParams = alertParams;
        return this;
    }

    public AlertData getAlertData() {
        return this.alertData;
    }

    public AlertInfo setAlertData(AlertData alertData) {
        this.alertData = alertData;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertInfo)) {
            return false;
        }
        final AlertInfo other = (AlertInfo) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object thisAlertParams = this.getAlertParams();
        final Object otherAlertParams = other.getAlertParams();
        if (!Objects.equals(thisAlertParams, otherAlertParams)) {
            return false;
        }
        final Object thisAlertData = this.getAlertData();
        final Object otherAlertData = other.getAlertData();
        return Objects.equals(thisAlertData, otherAlertData);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AlertInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        final Object alertParams = this.getAlertParams();
        result = result * prime + (alertParams == null ? 43 : alertParams.hashCode());
        final Object alertData = this.getAlertData();
        result = result * prime + (alertData == null ? 43 : alertData.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AlertInfo(alertParams=" + this.getAlertParams() + ", alertData=" + this.getAlertData() + ")";
    }

    public static class AlertInfoBuilder {
        private Map<String, String> alertParams;
        private AlertData alertData;

        AlertInfoBuilder() {
        }

        public AlertInfoBuilder alertParams(Map<String, String> alertParams) {
            this.alertParams = alertParams;
            return this;
        }

        public AlertInfoBuilder alertData(AlertData alertData) {
            this.alertData = alertData;
            return this;
        }

        public AlertInfo build() {
            return new AlertInfo(alertParams, alertData);
        }

        @Override
        public String toString() {
            return "AlertInfo.AlertInfoBuilder(alertParams=" + this.alertParams + ", alertData=" + this.alertData + ")";
        }
    }
}

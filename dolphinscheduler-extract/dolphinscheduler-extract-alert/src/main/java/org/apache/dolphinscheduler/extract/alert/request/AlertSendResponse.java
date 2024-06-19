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

package org.apache.dolphinscheduler.extract.alert.request;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSendResponse {

    /**
     * true:All alert are successful,
     * false:As long as one alert fails
     */
    private boolean success;

    private List<AlertSendResponseResult> resResults;

    public static AlertSendResponse success(List<AlertSendResponseResult> resResults) {
        return new AlertSendResponse(true, resResults);
    }

    public static AlertSendResponse fail(List<AlertSendResponseResult> resResults) {
        return new AlertSendResponse(false, resResults);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSendResponseResult implements Serializable {

        private boolean success;

        private String message;

        public static AlertSendResponseResult success() {
            return new AlertSendResponseResult(true, null);
        }

        public static AlertSendResponseResult fail(String message) {
            return new AlertSendResponseResult(false, message);
        }

    }

}

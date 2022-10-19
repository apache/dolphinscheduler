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

package org.apache.dolphinscheduler.plugin.alert.pagerduty;

public final class PagerDutyParamsConstants {

    public static final String NAME_PAGER_DUTY_INTEGRATION_KEY_NAME = "IntegrationKey";
    public static final String PAGER_DUTY_INTEGRATION_KEY = "integrationKey";
    public static final String PAGER_DUTY_EVENT_ACTION_TRIGGER = "trigger";
    public static final String PAGER_DUTY_EVENT_API = "https://events.pagerduty.com/v2/enqueue";
    public static final String PAGER_DUTY_EVENT_SOURCE = "DolphinScheduler";

    private PagerDutyParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

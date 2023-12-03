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

package org.apache.dolphinscheduler.plugin.alert.prometheus;

public class PrometheusAlertConstants {

    static final String ALERT_MANAGER_URL = "$t('url')";
    static final String NAME_ALERT_MANAGER_URL = "url";
    static final String ALERT_MANAGER_ANNOTATIONS = "$t('annotations')";
    static final String NAME_ALERT_MANAGER_ANNOTATIONS = "annotations";
    static final String ALERT_V2_API_PATH = "/api/v2/alerts";
    static final String GENERATOR_URL = "$t('generatorURL')";
    static final String NAME_GENERATOR_URL = "generatorURL";
    static final String ALERT_SUCCESS = "alert success";
}

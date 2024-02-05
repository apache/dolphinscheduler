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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class PrometheusAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Prometheus AlertManager";
    }

    @Override
    public List<PluginParams> params() {
        InputParam urlParam =
                InputParam
                        .newBuilder(PrometheusAlertConstants.NAME_ALERT_MANAGER_URL,
                                PrometheusAlertConstants.ALERT_MANAGER_URL)
                        .setPlaceholder(AlertInputTips.URL.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .build();
        InputParam annotationParam =
                InputParam
                        .newBuilder(PrometheusAlertConstants.NAME_ALERT_MANAGER_ANNOTATIONS,
                                PrometheusAlertConstants.ALERT_MANAGER_ANNOTATIONS)
                        .setPlaceholder(AlertInputTips.ANNOTATION.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();
        InputParam generatorUrlParam =
                InputParam
                        .newBuilder(PrometheusAlertConstants.NAME_GENERATOR_URL, PrometheusAlertConstants.GENERATOR_URL)
                        .setPlaceholder(AlertInputTips.GENERATOR_URL.getMsg())
                        .addValidate(Validate.newBuilder()
                                .setRequired(false).build())
                        .build();

        return Arrays.asList(urlParam, annotationParam, generatorUrlParam);
    }

    @Override
    public AlertChannel create() {
        return new PrometheusAlertChannel();
    }
}

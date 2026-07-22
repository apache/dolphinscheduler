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

package org.apache.dolphinscheduler.plugin.alert.http;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.input.number.InputNumberParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class HttpAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Http";
    }

    @Override
    public List<PluginParams> params() {

        InputParam url = InputParam.newBuilder(HttpAlertConstants.NAME_URL, HttpAlertConstants.URL)
                .setPlaceholder(JSONUtils.toJsonString(AlertInputTips.getAllMsg(AlertInputTips.URL)))
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam headerParams =
                InputParam.newBuilder(HttpAlertConstants.NAME_HEADER_PARAMS, HttpAlertConstants.HEADER_PARAMS)
                        .setPlaceholder(JSONUtils.toJsonString(AlertInputTips.getAllMsg(AlertInputTips.HEADER)))
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();

        RadioParam contentType =
                RadioParam.newBuilder(HttpAlertConstants.NAME_CONTENT_TYPE, HttpAlertConstants.CONTENT_TYPE)
                        .addParamsOptions(new ParamsOptions(OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue(),
                                OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue(), false))
                        .addParamsOptions(
                                new ParamsOptions(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED.getValue(),
                                        OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED.getValue(), false))
                        .setValue(OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue())
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .build();

        InputParam bodyParams =
                InputParam.newBuilder(HttpAlertConstants.NAME_BODY_PARAMS, HttpAlertConstants.BODY_PARAMS)
                        .setPlaceholder(JSONUtils.toJsonString(AlertInputTips.getAllMsg(AlertInputTips.JSON_BODY)))
                        .addValidate(Validate.newBuilder()
                                .setRequired(false)
                                .build())
                        .build();

        RadioParam requestType = RadioParam
                .newBuilder(HttpAlertConstants.NAME_REQUEST_TYPE, HttpAlertConstants.REQUEST_TYPE)
                .addParamsOptions(new ParamsOptions(HttpRequestMethod.GET.name(), HttpRequestMethod.GET.name(), false))
                .addParamsOptions(
                        new ParamsOptions(HttpRequestMethod.POST.name(), HttpRequestMethod.POST.name(), false))
                .addParamsOptions(new ParamsOptions(HttpRequestMethod.PUT.name(), HttpRequestMethod.PUT.name(), false))
                .setValue(HttpRequestMethod.GET.name())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputNumberParam timeout =
                InputNumberParam.newBuilder(HttpAlertConstants.NAME_TIMEOUT, HttpAlertConstants.TIMEOUT)
                        .setValue(HttpAlertConstants.DEFAULT_TIMEOUT)
                        .addValidate(Validate.newBuilder()
                                .setType(DataType.NUMBER.getDataType())
                                .setRequired(false)
                                .build())
                        .build();

        return Arrays.asList(url, requestType, headerParams, bodyParams, contentType, timeout);
    }

    @Override
    public AlertChannel create() {
        return new HttpAlertChannel();
    }
}

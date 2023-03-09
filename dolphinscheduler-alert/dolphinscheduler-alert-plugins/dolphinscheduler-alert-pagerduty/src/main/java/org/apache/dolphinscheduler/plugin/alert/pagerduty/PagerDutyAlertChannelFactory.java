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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.Collections;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class PagerDutyAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "PagerDuty";
    }

    @Override
    public List<PluginParams> params() {
        InputParam integrationKey = InputParam
                .newBuilder(PagerDutyParamsConstants.NAME_PAGER_DUTY_INTEGRATION_KEY_NAME,
                        PagerDutyParamsConstants.PAGER_DUTY_INTEGRATION_KEY)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        return Collections.singletonList(integrationKey);
    }

    @Override
    public AlertChannel create() {
        return new PagerDutyAlertChannel();
    }
}

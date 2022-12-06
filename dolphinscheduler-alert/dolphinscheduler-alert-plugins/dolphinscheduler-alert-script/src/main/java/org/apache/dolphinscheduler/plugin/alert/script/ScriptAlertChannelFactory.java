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

package org.apache.dolphinscheduler.plugin.alert.script;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class ScriptAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Script";
    }

    @Override
    public List<PluginParams> params() {

        InputParam scriptUserParam = InputParam
                .newBuilder(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS, ScriptParamsConstants.SCRIPT_USER_PARAMS)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .setPlaceholder(AlertInputTips.CUSTOMIZED_PARAMS.getMsg())
                .build();
        // need check file type and file exist
        InputParam scriptPathParam =
                InputParam.newBuilder(ScriptParamsConstants.NAME_SCRIPT_PATH, ScriptParamsConstants.SCRIPT_PATH)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .build())
                        .setPlaceholder(AlertInputTips.SCRIPT_PATH.getMsg())
                        .build();

        RadioParam scriptTypeParams = RadioParam
                .newBuilder(ScriptParamsConstants.NAME_SCRIPT_TYPE, ScriptParamsConstants.SCRIPT_TYPE)
                .addParamsOptions(new ParamsOptions(ScriptType.SHELL.getDescp(), ScriptType.SHELL.getDescp(), false))
                .setValue(ScriptType.SHELL.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        return Arrays.asList(scriptUserParam, scriptPathParam, scriptTypeParams);
    }

    @Override
    public AlertChannel create() {
        return new ScriptAlertChannel();
    }
}

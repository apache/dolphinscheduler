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

package org.apache.dolphinscheduler.plugin.alert.voice;

import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_ACCESS_KEY_ID;
import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_ACCESS_KEY_SECRET;
import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_ADDRESS;
import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_CALLED_NUMBER;
import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_CALLED_SHOW_NUMBER;
import static org.apache.dolphinscheduler.plugin.alert.voice.VoiceAlertConstants.NAME_TTS_CODE;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class VoiceAlertChannel implements AlertChannel {

    @Override
    public AlertResult process(AlertInfo info) {

        Map<String, String> paramsMap = info.getAlertParams();
        if (null == paramsMap) {
            return new AlertResult(false, "aliyun-voice params is null");
        }
        VoiceParam voiceParam = buildVoiceParam(paramsMap);
        return new VoiceSender(voiceParam).send();
    }

    public VoiceParam buildVoiceParam(Map<String, String> paramsMap) {
        String calledNumber = paramsMap.get(NAME_CALLED_NUMBER);
        String calledShowNumber = paramsMap.get(NAME_CALLED_SHOW_NUMBER);
        String ttsCode = paramsMap.get(NAME_TTS_CODE);
        VoiceParam param = new VoiceParam();
        param.setCalledNumber(calledNumber);
        param.setCalledShowNumber(calledShowNumber);
        param.setTtsCode(ttsCode);
        VoiceParam.Connection connection = new VoiceParam.Connection();
        String address = paramsMap.get(NAME_ADDRESS);
        String accessKeyId = paramsMap.get(NAME_ACCESS_KEY_ID);
        String accessKeySecret = paramsMap.get(NAME_ACCESS_KEY_SECRET);
        connection.setAddress(address);
        connection.setAccessKeyId(accessKeyId);
        connection.setAccessKeySecret(accessKeySecret);
        param.setConnection(connection);

        // set callback ID
        param.setOutId(calledShowNumber);
        return param;
    }
}

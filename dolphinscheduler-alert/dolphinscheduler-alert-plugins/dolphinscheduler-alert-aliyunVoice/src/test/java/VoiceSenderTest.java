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

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.plugin.alert.voice.VoiceParam;
import org.apache.dolphinscheduler.plugin.alert.voice.VoiceSender;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * VoiceSenderTest
 */
class VoiceSenderTest {

    private static VoiceParam voiceParam = new VoiceParam();

    @BeforeEach
    void initVoidceParam() {
        voiceParam.setCalledNumber("12345678910");
        voiceParam.setTtsParam("TTS_2450XXXXX");
        VoiceParam.Connection connection = new VoiceParam.Connection();
        connection.setAddress("xxxxxx.aliyuncs.com");
        connection.setAccessKeyId("XXXXXXXXX");
        connection.setAccessKeySecret("XXXXXXXXX");
        voiceParam.setConnection(connection);
    }

    @Test
    void testSendWeChatTableMsg() {
        VoiceSender weChatSender = new VoiceSender(voiceParam);

        AlertResult alertResult = weChatSender.send();
        Assertions.assertFalse(alertResult.isSuccess());
    }

}

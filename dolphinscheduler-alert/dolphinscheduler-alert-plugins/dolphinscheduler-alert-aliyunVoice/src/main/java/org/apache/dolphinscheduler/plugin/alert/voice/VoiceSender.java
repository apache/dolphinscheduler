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

import org.apache.dolphinscheduler.alert.api.AlertResult;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

@Slf4j
public final class VoiceSender {

    private VoiceParam voiceParam;

    /**
     * create Client
     *
     * @param voiceParam voidce Param
     * @return Client
     * @throws Exception
     */
    public VoiceSender(VoiceParam voiceParam) {
        this.voiceParam = voiceParam;
    }

    public AlertResult send() {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);
        try {
            Client client = createClient(voiceParam.getConnection());
            SingleCallByTtsRequest singleCallByTtsRequest = new SingleCallByTtsRequest()
                    .setCalledNumber(voiceParam.getCalledNumber())
                    .setTtsCode(voiceParam.getTtsCode())
                    .setOutId(voiceParam.getOutId());
            RuntimeOptions runtime = new RuntimeOptions();
            SingleCallByTtsResponse response = client.singleCallByTtsWithOptions(singleCallByTtsRequest, runtime);
            if (response == null) {
                alertResult.setMessage("aliyun-vocie response is null");
                return alertResult;
            }
            SingleCallByTtsResponseBody body = response.getBody();
            if (body.code.equalsIgnoreCase("ok")) {
                alertResult.setSuccess(true);
                alertResult.setMessage(body.getCallId());
            } else {
                alertResult.setMessage(body.getMessage());
            }
        } catch (Exception e) {
            log.error("send aliyun vocie error.", e);
            alertResult.setMessage(e.getMessage());
        }
        return alertResult;
    }

    /**
     * create Client
     *
     * @param conn conn info
     * @return Client
     * @throws Exception
     */
    private com.aliyun.dyvmsapi20170525.Client createClient(VoiceParam.Connection conn) throws Exception {
        Config config = new Config()
                // AccessKey ID
                .setAccessKeyId(conn.getAccessKeyId())
                // AccessKey Secret
                .setAccessKeySecret(conn.getAccessKeySecret());
        // address
        config.endpoint = conn.getAddress();
        return new com.aliyun.dyvmsapi20170525.Client(config);
    }

    public VoiceParam getVoidcePara() {
        return voiceParam;
    }

    public void setVoidcePara(VoiceParam voidcePara) {
        this.voiceParam = voidcePara;
    }
}

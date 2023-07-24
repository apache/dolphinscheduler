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

    private VoidceParam voidceParam;

    /**
     * create Client
     *
     * @param voidceParam voidce Param
     * @return Client
     * @throws Exception
     */
    public VoiceSender(VoidceParam voidceParam) {
        this.voidceParam = voidceParam;
    }

    public AlertResult send() {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");
        try {
            Client client = createClient(voidceParam.getConnection());
            SingleCallByTtsRequest singleCallByTtsRequest = new SingleCallByTtsRequest()
                    .setCalledNumber(voidceParam.getCalledNumber())
                    .setTtsCode(voidceParam.getTtsCode())
                    .setOutId(voidceParam.getOutId());
            RuntimeOptions runtime = new RuntimeOptions();
            SingleCallByTtsResponse response = client.singleCallByTtsWithOptions(singleCallByTtsRequest, runtime);
            if (response == null) {
                alertResult.setMessage("vocie send error.");
                return alertResult;
            }
            SingleCallByTtsResponseBody body = response.getBody();
            alertResult.setStatus(body.code.toLowerCase().equals("ok") ? "true" : "false");
        } catch (Exception e) {
            log.error("vocie send error.", e);
            alertResult.setMessage("vocie send error.");
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
    private com.aliyun.dyvmsapi20170525.Client createClient(VoidceParam.Connection conn) throws Exception {
        Config config = new Config()
                // AccessKey ID
                .setAccessKeyId(conn.getAccessKeyId())
                // AccessKey Secret
                .setAccessKeySecret(conn.getAccessKeySecret());
        // address
        config.endpoint = conn.getAddress();
        return new com.aliyun.dyvmsapi20170525.Client(config);
    }

    public VoidceParam getVoidcePara() {
        return voidceParam;
    }

    public void setVoidcePara(VoidceParam voidcePara) {
        this.voidceParam = voidcePara;
    }
}

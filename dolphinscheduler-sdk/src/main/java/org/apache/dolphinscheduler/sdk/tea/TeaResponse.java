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

package org.apache.dolphinscheduler.sdk.tea;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Response;

public class TeaResponse {
    public Response response;
    public int statusCode;
    public String statusMessage;
    public HashMap<String, String> headers;
    public InputStream body;

    public TeaResponse() {
        headers = new HashMap<String, String>();
    }

    public TeaResponse(Response response) {
        headers = new HashMap<String, String>();
        this.response = response;
        statusCode = response.code();
        statusMessage = response.message();
        body = response.body().byteStream();
        Headers headers = response.headers();
        Map<String, List<String>> resultHeaders = headers.toMultimap();
        for (Map.Entry<String, List<String>> entry : resultHeaders.entrySet()) {
            this.headers.put(entry.getKey(), StringUtils.join(";", entry.getValue()));
        }
    }

    public InputStream getResponse() {
        return this.body;
    }

    public String getResponseBody() {
        if (null == body) {
            return String.format("{\"message\":\"%s\"}", statusMessage);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        try {
            while (true) {
                final int read = body.read(buff);
                if (read == -1) {
                    break;
                }
                os.write(buff, 0, read);
            }
        } catch (Exception e) {
            throw new TeaException(e.getMessage(), e);
        }
        return new String(os.toByteArray());
    }
}

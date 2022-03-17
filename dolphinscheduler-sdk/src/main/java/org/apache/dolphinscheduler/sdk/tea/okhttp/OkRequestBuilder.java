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

package org.apache.dolphinscheduler.sdk.tea.okhttp;

import org.apache.dolphinscheduler.sdk.tea.TeaConverter;
import org.apache.dolphinscheduler.sdk.tea.TeaException;
import org.apache.dolphinscheduler.sdk.tea.TeaPair;
import org.apache.dolphinscheduler.sdk.tea.TeaRequest;

import java.net.URL;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkRequestBuilder {
    private Request.Builder builder;

    public OkRequestBuilder(Request.Builder builder) {
        this.builder = builder;
    }

    public OkRequestBuilder url(URL url) {
        this.builder.url(url);
        return this;
    }

    public OkRequestBuilder header(Map<String, String> headers) {
        for (String headerName : headers.keySet()) {
            this.builder.header(headerName, headers.get(headerName));
        }
        return this;
    }

    public Request buildRequest(TeaRequest request) {
        String method = request.method.toUpperCase();
        OkRequestBody requestBody;
        switch (method) {
            case "DELETE":
                this.builder.delete();
                break;
            case "POST":
                requestBody = new OkRequestBody(request);
                this.builder.post(requestBody);
                break;
            case "PUT":
                requestBody = new OkRequestBody(request);
                this.builder.put(requestBody);
                break;
            case "PATCH":
                requestBody = new OkRequestBody(request);
                this.builder.patch(requestBody);
                break;
            default:
                this.builder.get();
                break;
        }
        return this.builder.build();
    }

    public Request buildMultipartFileRequest(TeaRequest request) {
        String method = request.method.toUpperCase();
        OkRequestBody requestBod0 = new OkRequestBody(request);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", request.query.get("fileName"), requestBod0)
                .build();
        switch (method) {
            case "POST":
                this.builder.post(requestBody);
                break;
            case "PUT":
                this.builder.put(requestBody);
                break;
            case "PATCH":
                this.builder.patch(requestBody);
                break;
            default:
                throw new TeaException(TeaConverter.buildMap(
                        new TeaPair("code", "buildMultipartFileRequest error"),
                        new TeaPair("message", String.format("'method'  %s not supported", method))
                ));
        }
        return this.builder.build();
    }
}

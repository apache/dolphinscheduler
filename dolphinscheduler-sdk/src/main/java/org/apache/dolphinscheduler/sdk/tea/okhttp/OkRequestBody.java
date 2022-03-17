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

import org.apache.dolphinscheduler.sdk.tea.StringUtils;
import org.apache.dolphinscheduler.sdk.tea.TeaRequest;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class OkRequestBody extends RequestBody {

    private InputStream inputStream;
    private String contentType;

    public OkRequestBody(TeaRequest teaRequest) {
        this.inputStream = teaRequest.body;
        this.contentType = teaRequest.headers.get("content-type");
    }

    public OkRequestBody(InputStream inputStream, String contentType) {
        this.inputStream = inputStream;
        this.contentType = contentType;
    }


    @Override
    public MediaType contentType() {
        MediaType type;
        if (StringUtils.isEmpty(contentType)) {
            if (null == inputStream) {
                return null;
            }
            type = MediaType.parse("application/json; charset=UTF-8;");
            return type;
        }
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() throws IOException {
        if (null != inputStream && inputStream.available() > 0) {
            return inputStream.available();
        }
        return super.contentLength();
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        if (null == inputStream) {
            return;
        }
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            bufferedSink.write(buffer, 0, bytesRead);
        }
    }
}


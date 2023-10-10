/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.alert.api;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

public class HttpServiceRetryStrategy implements HttpRequestRetryHandler {

    public static final HttpServiceRetryStrategy retryStrategy = new HttpServiceRetryStrategy();

    private static final int RETRY_COUNT = 3;

    private static final long RETRY_INTERVAL_TIME = 2000L;

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
        if (executionCount > RETRY_COUNT) {
            return false;
        }

        if (exception instanceof SSLException) {
            return false;
        }

        if (exception instanceof UnknownHostException ||
                exception instanceof InterruptedIOException
                || exception instanceof NoHttpResponseException
                || exception instanceof SocketException) {
            // retry interval time
            try {
                Thread.sleep(RETRY_INTERVAL_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }

        HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
        HttpRequest request = clientContext.getRequest();

        // Retry if the request is considered idempotent
        return !(request instanceof HttpEntityEnclosingRequest);
    }
}

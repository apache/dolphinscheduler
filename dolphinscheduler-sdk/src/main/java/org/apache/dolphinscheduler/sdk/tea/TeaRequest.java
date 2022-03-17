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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TeaRequest {
    public final static String URL_ENCODING = "UTF-8";

    public String protocol;

    public Integer port;

    public String method;

    public String pathname;

    public Map<String, String> query;

    public Map<String, String> headers;

    public InputStream body;

    public TeaRequest() {
        protocol = "http";
        method = "GET";
        query = new HashMap<String, String>();
        headers = new HashMap<String, String>();
    }

    public static TeaRequest create() {
        return new TeaRequest();
    }

    @Override
    public String toString() {
        String output = "Protocol: " + this.protocol + "\nPort: " + this.port + "\n" + this.method + " " + this.pathname
                + "\n";
        output += "Query:\n";
        for (Map.Entry<String, String> e : this.query.entrySet()) {
            output += "    " + e.getKey() + ": " + e.getValue() + "\n";
        }
        output += "Headers:\n";
        for (Map.Entry<String, String> e : this.headers.entrySet()) {
            output += "    " + e.getKey() + ": " + e.getValue() + "\n";
        }
        return output;
    }
}

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

package org.apache.dolphinscheduler.sdk;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

import java.util.Map;

public class Params extends TeaModel {
    @NameInMap("action")
    @Validation(required = true)
    public String action = "dolphinScheduler";
    @NameInMap("version")
    @Validation(required = true)
    public String version = "2021-11-08";
    @NameInMap("protocol")
    @Validation(required = false)
    public String protocol;
    @NameInMap("pathname")
    @Validation(required = false)
    public String pathname;
    @NameInMap("method")
    @Validation(required = true)
    public String method = "GET"; // PUT,DELETE,POST,PATCH
    @NameInMap("authType")
    @Validation(required = true)
    public String authType = "sts"; // token方式验证，放在headers里面
    @NameInMap("bodyType")
    @Validation(required = true)
    public String bodyType = "json";
    @NameInMap("reqBodyType")
    @Validation(required = true)
    public String reqBodyType = "json";
    @NameInMap("style")
    @Validation(required = true)
    public String style = "ROA";

    public Params() {
    }

    public static Params build(Map<String, ?> map) throws Exception {
        Params self = new Params();
        return (Params) TeaModel.build(map, self);
    }

    public Params setAction(String action) {
        this.action = action;
        return this;
    }

    public String getAction() {
        return this.action;
    }

    public Params setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public Params setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public Params setPathname(String pathname) {
        this.pathname = pathname;
        return this;
    }

    public String getPathname() {
        return this.pathname;
    }

    public Params setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getMethod() {
        return this.method;
    }

    public Params setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public String getAuthType() {
        return this.authType;
    }

    public Params setBodyType(String bodyType) {
        this.bodyType = bodyType;
        return this;
    }

    public String getBodyType() {
        return this.bodyType;
    }

    public Params setReqBodyType(String reqBodyType) {
        this.reqBodyType = reqBodyType;
        return this;
    }

    public String getReqBodyType() {
        return this.reqBodyType;
    }

    public Params setStyle(String style) {
        this.style = style;
        return this;
    }

    public String getStyle() {
        return this.style;
    }
}

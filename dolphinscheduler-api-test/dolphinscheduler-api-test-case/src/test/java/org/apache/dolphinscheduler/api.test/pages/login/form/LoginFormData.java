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


package org.apache.dolphinscheduler.api.test.pages.login.form;

public enum LoginFormData {
    USR_NAME("userName", "admin"),
    USER_PASSWD("userPassword", "dolphinscheduler123");

    LoginFormData(String param, String data) {
        this.data = data;
        this.param = param;

    }

    private final String param;
    private final String data;

    public String getParam() {
        return param;
    }

    public String getData() {
        return data;
    }
}

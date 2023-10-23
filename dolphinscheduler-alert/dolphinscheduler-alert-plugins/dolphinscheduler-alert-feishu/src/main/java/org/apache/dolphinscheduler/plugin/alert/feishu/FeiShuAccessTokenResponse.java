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
package org.apache.dolphinscheduler.plugin.alert.feishu;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeiShuAccessTokenResponse {

    private int code;
    private String msg;

    @JsonProperty("tenant_access_token")
    private String tenantAccessToken;
    private int expire;

    public FeiShuAccessTokenResponse() {
    }

    public void setCode(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }

    public void setTenantAccessToken(String tenantAccessToken) {
        this.tenantAccessToken = tenantAccessToken;
    }
    public String getTenantAccessToken() {
        return tenantAccessToken;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }
    public int getExpire() {
        return expire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FeiShuAccessTokenResponse that = (FeiShuAccessTokenResponse) o;
        return code == that.code && expire == that.expire && Objects.equals(msg, that.msg)
                && Objects.equals(tenantAccessToken, that.tenantAccessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, tenantAccessToken, expire);
    }
}

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

package org.apache.dolphinscheduler.api.test.pages;

public class Route {

    public static String login() {
        return "/login";
    }

    public static String loginOut() {
        return "/signOut";
    }

    public static String tenants() {
        return "/tenants";
    }

    public static String tenants(int id) {
        return "/tenants/" + String.valueOf(id);
    }

    public static String tenantsList() {
        return "/tenants/list";
    }

    public static String tenantsVerifyCode() {
        return "/tenants/verify-code";
    }

    public static String accessTokens() {
        return "/access-tokens";
    }

    public static String accessTokens(int id) {
        return "/access-tokens";
    }

    public static String accessTokenByUser(int userId) {
        return accessTokens() + "/user/" + String.valueOf(userId);
    }

    public static String generateAccessToken() {
        return accessTokens() + "/generate";
    }


}

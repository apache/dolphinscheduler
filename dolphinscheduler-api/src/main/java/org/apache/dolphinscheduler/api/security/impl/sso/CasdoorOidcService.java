
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.security.impl.sso;

import lombok.RequiredArgsConstructor;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CasdoorOidcService implements OidcService {

    private final CasdoorAuthService casdoorAuthService;

    @Override
    public String getToken(String code, String state) {
        return casdoorAuthService.getOAuthToken(code, state);
    }

    @Override
    public OidcUserInfo getUserInfo(String token) {
        CasdoorUser casdoorUser = casdoorAuthService.parseJwtToken(token);
        return new OidcUserInfo(casdoorUser.getName(), casdoorUser.getEmail());
    }

    @Override
    public String getSignInUrl(String redirectUrl, String state) {
        return casdoorAuthService.getSigninUrl(redirectUrl, state);
    }
}

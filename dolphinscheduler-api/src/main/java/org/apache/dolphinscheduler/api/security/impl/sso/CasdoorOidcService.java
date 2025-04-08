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

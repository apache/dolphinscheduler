package org.apache.dolphinscheduler.api.security.impl.sso;

public interface OidcService {
    String getToken(String code, String state);
    OidcUserInfo getUserInfo(String token);
    String getSignInUrl(String redirectUrl, String state);
}

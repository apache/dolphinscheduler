package org.apache.dolphinscheduler.oauth;

public interface AuthorizeCodeAuthService {

    /**
     * The user login with an authorization code and retrieves user information to generate a user in DS.
     */
    OAuthUserInfo getUserInfo(String authorizationCode);
}

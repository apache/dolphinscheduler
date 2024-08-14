package org.apache.dolphinscheduler.oauth;

import com.google.auto.service.AutoService;

/** Factory of {@link GiteeAuthorizeCodeAuthService} */
@AutoService(OAuthServiceFactory.class)
public class GiteeAuthorizeCodeAuthServiceFactory implements OAuthServiceFactory {

    @Override
    public String provider() {
        return GiteeOAuthProviderConstants.GITEE_OAUTH_PROVIDER_NAME;
    }

    @Override
    public AuthorizeCodeAuthService getAuthorizeCodeAuthService(OAuth2ClientProperties oAuth2ClientProperties) {
        return new GiteeAuthorizeCodeAuthService(oAuth2ClientProperties);
    }
}

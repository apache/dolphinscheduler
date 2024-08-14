package org.apache.dolphinscheduler.oauth;

import com.google.auto.service.AutoService;

/** Factory of {@link GithubAuthorizeCodeAuthService} */
@AutoService(OAuthServiceFactory.class)
public class GithubAuthorizeCodeAuthServiceFactory implements OAuthServiceFactory {

    @Override
    public String provider() {
        return GithubOAuthProviderConstants.GITHUB_OAUTH_PROVIDER_NAME;
    }

    @Override
    public AuthorizeCodeAuthService getAuthorizeCodeAuthService(OAuth2ClientProperties oAuth2ClientProperties) {
        return new GithubAuthorizeCodeAuthService(oAuth2ClientProperties);
    }
}

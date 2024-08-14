package org.apache.dolphinscheduler.oauth;

/** Factory of {@link AuthorizeCodeAuthService} */
public interface OAuthServiceFactory {

    /**
     * OAuth2 provider name
     */
    String provider();

    /**
     * Create authorizeCodeAuthService.
     */
    AuthorizeCodeAuthService getAuthorizeCodeAuthService(OAuth2ClientProperties oAuth2ClientProperties);
}

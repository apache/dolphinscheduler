package org.apache.dolphinscheduler.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2ClientProperties {

    private String authorizationUri;
    private String clientId;
    private String redirectUri;
    private String clientSecret;
    private String tokenUri;
    private String userInfoUri;
    private String callbackUrl;
    private String iconUri;
    private String provider;

}

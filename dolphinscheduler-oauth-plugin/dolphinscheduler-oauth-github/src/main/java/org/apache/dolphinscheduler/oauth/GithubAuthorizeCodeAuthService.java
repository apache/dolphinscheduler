package org.apache.dolphinscheduler.oauth;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * GitHub oauth2 provider.  <a href="https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/differences-between-github-apps-and-oauth-apps">GitHub oauth docs</a>
 */
public class GithubAuthorizeCodeAuthService implements AuthorizeCodeAuthService {

    private OAuth2ClientProperties oAuth2ClientProperties;

    public GithubAuthorizeCodeAuthService(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    @Override
    @SneakyThrows
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo();
        Map<String, String> tokenRequestHeader = new HashMap<>();
        tokenRequestHeader.put("Accept", "application/json");
        Map<String, Object> requestBody = new HashMap<>(16);
        requestBody.put("client_secret", oAuth2ClientProperties.getClientSecret());
        HashMap<String, Object> requestParamsMap = new HashMap<>();
        requestParamsMap.put("client_id", oAuth2ClientProperties.getClientId());
        requestParamsMap.put("code", authorizationCode);
        requestParamsMap.put("grant_type", "authorization_code");
        requestParamsMap.put("redirect_uri",
                String.format("%s?provider=%s", oAuth2ClientProperties.getRedirectUri(),
                        GithubOAuthProviderConstants.GITHUB_OAUTH_PROVIDER_NAME));
        String tokenJsonStr = OkHttpUtils.post(oAuth2ClientProperties.getTokenUri(), tokenRequestHeader,
                requestParamsMap, requestBody);
        String accessToken = JSONUtils.getNodeString(tokenJsonStr, "access_token");
        Map<String, String> userInfoRequestHeaders = new HashMap<>();
        userInfoRequestHeaders.put("Accept", "application/json");
        Map<String, Object> userInfoQueryMap = new HashMap<>();
        userInfoRequestHeaders.put("Authorization", "Bearer " + accessToken);
        String userInfoJsonStr =
                OkHttpUtils.get(oAuth2ClientProperties.getUserInfoUri(), userInfoRequestHeaders, userInfoQueryMap);
        String username = JSONUtils.getNodeString(userInfoJsonStr, "login");
        String email = JSONUtils.getNodeString(userInfoJsonStr, "email");
        String name = JSONUtils.getNodeString(userInfoJsonStr, "name");
        oAuthUserInfo.setUsername(username);
        oAuthUserInfo.setName(name);
        oAuthUserInfo.setEmail(email);
        return oAuthUserInfo;
    }
}

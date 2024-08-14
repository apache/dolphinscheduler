package org.apache.dolphinscheduler.oauth;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * Gitee oauth2 provider.  <a href="https://gitee.com/api/v5/oauth_doc#/">Gitee oauth docs</a>
 */
public class GiteeAuthorizeCodeAuthService implements AuthorizeCodeAuthService {

    private OAuth2ClientProperties oAuth2ClientProperties;

    public GiteeAuthorizeCodeAuthService(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    @Override
    @SneakyThrows
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo();
        Map<String, String> tokenRequestHeader = new HashMap<>();
        tokenRequestHeader.put(OAuthConstants.ACCEPT, OAuthConstants.APPLICATION_JSON);
        Map<String, Object> requestBody = new HashMap<>(16);
        requestBody.put(OAuthConstants.CLIENT_SECRET, oAuth2ClientProperties.getClientSecret());
        HashMap<String, Object> requestParamsMap = new HashMap<>();
        requestParamsMap.put(OAuthConstants.CLIENT_ID, oAuth2ClientProperties.getClientId());
        requestParamsMap.put(OAuthConstants.CODE, authorizationCode);
        requestParamsMap.put(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        requestParamsMap.put(OAuthConstants.REDIRECT_URI,
                String.format(GiteeOAuthProviderConstants.TOKEN_URI_FORMAT, oAuth2ClientProperties.getRedirectUri(),
                        GiteeOAuthProviderConstants.GITEE_OAUTH_PROVIDER_NAME));
        String tokenJsonStr = OkHttpUtils.post(oAuth2ClientProperties.getTokenUri(), tokenRequestHeader,
                requestParamsMap, requestBody);
        String accessToken = JSONUtils.getNodeString(tokenJsonStr, OAuthConstants.ACCESS_TOKEN);
        Map<String, String> userInfoRequestHeaders = new HashMap<>();
        userInfoRequestHeaders.put(OAuthConstants.ACCEPT, OAuthConstants.APPLICATION_JSON);
        Map<String, Object> userInfoQueryMap = new HashMap<>();
        userInfoQueryMap.put(OAuthConstants.ACCESS_TOKEN, accessToken);
        userInfoRequestHeaders.put(OAuthConstants.AUTHORIZATION_HEADER, OAuthConstants.BEARER + " " + accessToken);
        String userInfoJsonStr =
                OkHttpUtils.get(oAuth2ClientProperties.getUserInfoUri(), userInfoRequestHeaders, userInfoQueryMap);
        String username =
                JSONUtils.getNodeString(userInfoJsonStr, GiteeOAuthProviderConstants.USER_INFO_USERNAME_PARAMETER);
        String name = JSONUtils.getNodeString(userInfoJsonStr, GiteeOAuthProviderConstants.USER_INFO_NAME_PARAMETER);
        String email = JSONUtils.getNodeString(userInfoJsonStr, GiteeOAuthProviderConstants.USER_INFO_EMAIL_PARAMETER);
        oAuthUserInfo.setUsername(username);
        oAuthUserInfo.setName(name);
        oAuthUserInfo.setEmail(email);
        return oAuthUserInfo;
    }
}

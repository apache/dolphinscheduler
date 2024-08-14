package org.apache.dolphinscheduler.oauth;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GiteeOAuthProviderConstants {

    public static final String GITEE_OAUTH_PROVIDER_NAME = "gitee";

    public static final String USER_INFO_USERNAME_PARAMETER = "login";

    public static final String USER_INFO_EMAIL_PARAMETER = "email";

    public static final String USER_INFO_NAME_PARAMETER = "name";

    public static final String TOKEN_URI_FORMAT = "%s?provider=%s";
}

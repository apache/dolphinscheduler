package org.apache.dolphinscheduler.api.security.impl.oauth2;

import org.apache.dolphinscheduler.api.security.impl.AbstractAuthenticator;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2Authenticator extends AbstractAuthenticator {

    private static Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Override
    public synchronized User login(String userId, String password, Object extra) {
        OAuth2User principal = (OAuth2User) extra;
        final String rawName = principal.getAttribute("name");
        final String userName = rawName.replaceAll(" ", "_");
        User user = null;

        if (userName != null) {
            // check if user exist
            user = userService.getUserByUserName(userName);
            if (user == null) {
                user = userService.createUser(UserType.GENERAL_USER, userName, "test@invalid.com");
            }
        }
        return user;
    }
}

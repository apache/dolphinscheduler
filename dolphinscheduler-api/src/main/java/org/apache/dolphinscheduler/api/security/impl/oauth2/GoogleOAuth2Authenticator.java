package org.apache.dolphinscheduler.api.security.impl.oauth2;

import org.apache.dolphinscheduler.api.security.impl.AbstractAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.ldap.LdapService;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class GoogleOAuth2Authenticator extends AbstractAuthenticator {

    private static Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Autowired
    LdapService GoogleOAuth2Service;

    @Override
    public User login(String userId, String password, Object extra) {
        logger.info("[debug111] extra: {}", extra.toString());
        OAuth2User principal = (OAuth2User) extra;
        final String rawName = principal.getAttribute("name");
        final String userName = rawName.replaceAll(" ", "_");
        logger.info("[debug111] userName: {}", userName);
        User user = null;

        if (userName != null) {
            // check if user exist
            user = userService.getUserByUserName(userName);
            if (user == null) {
                logger.info("[debug111] userName not exists in db: {}", userName);
                user = userService.createUser(UserType.GENERAL_USER, userName, "test@invalid.com");
                logger.info("[debug111] user created with userName: {}", userName);
            }
        }
        return user;
    }
}

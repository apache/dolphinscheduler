package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.pages.security.user.UserPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateRequestEntity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DisplayName("User Page API test")
public class UserAPITest extends AbstractAPITest {
    private UserPageAPI userPageAPI = null;
    private UserCreateRequestEntity userCreateRequestEntity;

    @BeforeAll
    public void initUserPageAPIFactory() {
        userPageAPI = pageAPIFactory.createUserPageAPI();
        userCreateRequestEntity = new UserCreateRequestEntity();
        userCreateRequestEntity.setUserName("shimin.an3");
        userCreateRequestEntity.setUserPassword(Constants.USER_PASSWD);
        userCreateRequestEntity.setEmail("admin@gmail.com");
    }

    @Test
    @Order(1)
    public void testUserToken() {
        userPageAPI.createUser(userCreateRequestEntity).isResponseSuccessful();
    }
}

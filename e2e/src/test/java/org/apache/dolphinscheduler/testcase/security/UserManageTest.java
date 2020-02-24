package org.apache.dolphinscheduler.testcase.security;

import org.apache.dolphinscheduler.base.BaseTest;
import org.apache.dolphinscheduler.page.security.UserManagePage;
import org.testng.annotations.Test;

public class UserManageTest extends BaseTest {
    private UserManagePage userManagePage;

    @Test(description = "TenantTest", priority = 1)
    public void testUserManage() throws InterruptedException {
        userManagePage = new UserManagePage(driver, redisUtil);
        // enter user manage page
        userManagePage.jumpPage();
        //assert user manage page
        assert userManagePage.creatUser();
    }
}

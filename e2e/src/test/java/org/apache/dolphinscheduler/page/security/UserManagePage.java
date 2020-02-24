package org.apache.dolphinscheduler.page.security;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.constant.TestConstant;
import org.apache.dolphinscheduler.data.LoginData;
import org.apache.dolphinscheduler.data.security.UserManageData;
import org.apache.dolphinscheduler.locator.LoginLocator;
import org.apache.dolphinscheduler.locator.security.UserManageLocator;
import org.apache.dolphinscheduler.util.RedisUtil;
import org.openqa.selenium.WebDriver;

public class UserManagePage extends PageCommon {
    public UserManagePage(WebDriver driver, RedisUtil redisUtil) {
        super(driver, redisUtil);
    }
    /**
     * jump page
     */
    public void jumpPage() {
        System.out.println("jump tenant page");
        super.jumpPage(UserManageData.USER_URL);
    }

    /**
     * creatTenant
     *
     * @return Whether to enter the specified page after creat tenant
     */
    public boolean creatUser() throws InterruptedException {
        Thread.sleep(TestConstant.ONE_THOUSANG);

        //click  USERMANAGE
        clickElement(UserManageLocator.CLICK_USERMANAGE);

        Thread.sleep(TestConstant.ONE_THOUSANG);

        //click  create user button
        clickButton(UserManageLocator.CLICK_CREATE_USER_BUTTON);

        // input user data
        sendInput(UserManageLocator.INPUT_USERNAME, UserManageData.USERNAME);
        sendInput(UserManageLocator.INPUT_PASSWORD, UserManageData.PASSWORD);
        clickButton(UserManageLocator.CLICK_TENANT);
        clickButton(UserManageLocator.SELECT_TENANT);
        clickButton(UserManageLocator.CLICK_QUEUE);
        clickButton(UserManageLocator.SELECT_QUEUE);
        sendInput(UserManageLocator.TENANT_INPUT_EMAIL, UserManageData.EMAIL);
        sendInput(UserManageLocator.TENANT_INPUT_PHONE, UserManageData.PHONE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }
}

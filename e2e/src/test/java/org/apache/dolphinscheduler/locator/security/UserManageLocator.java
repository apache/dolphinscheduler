package org.apache.dolphinscheduler.locator.security;

import org.openqa.selenium.By;

public class UserManageLocator {

    public static final By CLICK_USERMANAGE = By.xpath("//div[3]/div/a/div/a/span");

    public static final By CLICK_CREATE_USER_BUTTON = By.xpath("//span[contains(.,'创建用户')]");

    public static final By INPUT_USERNAME = By.xpath("//div[2]/div/div/div[2]/div/input");

    public static final By INPUT_PASSWORD = By.xpath("//div[2]/div[2]/div/input");

    public static final By CLICK_TENANT = By.xpath("//div[3]/div[2]/div/div/div/input");


    public static final By SELECT_TENANT = By.xpath("//div[3]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By CLICK_QUEUE = By.xpath("//div[4]/div[2]/div/div/div/input");

    public static final By SELECT_QUEUE = By.xpath("//div[4]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By TENANT_INPUT_EMAIL = By.xpath("//div[5]/div[2]/div/input");

    public static final By TENANT_INPUT_PHONE = By.xpath("//div[6]/div[2]/div/input");

    public static final By SUBMIT = By.xpath("//div[3]/button[2]/span");

}

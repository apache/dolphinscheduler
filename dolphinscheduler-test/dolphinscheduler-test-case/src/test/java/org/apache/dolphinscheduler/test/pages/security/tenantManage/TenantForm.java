package org.apache.dolphinscheduler.test.pages.security.tenantManage;

import org.apache.dolphinscheduler.test.core.Module;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

public class TenantForm extends Module {

    @FindBys({
            @FindBy(className = "input-tenant-code"),
            @FindBy(tagName = "input"),
    })
    private WebElement inputTenantCode;

    @FindBy(className = "select-queue")
    private WebElement selectQueue;

    @FindBys({
            @FindBy(className = "input-description"),
            @FindBy(tagName = "textarea"),
    })
    private WebElement inputDescription;

    @FindBy(className = "btn-submit")
    private WebElement buttonSubmit;

    @FindBy(className = "btn-cancel")
    private WebElement buttonCancel;
    
    public TenantForm() {}

    public void create(String tenant, String description) {
        inputTenantCode.sendKeys(tenant);
        inputDescription.sendKeys(description);
        buttonSubmit.click();
    }

}

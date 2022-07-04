package org.apache.dolphinscheduler.test.pages.security.tenantManage;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.pages.security.SecuritySidePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


public class TenantManagePage extends SecuritySidePage {
    private TenantForm tenantForm;
    @FindBy(className = "btn-create-tenant")
    private WebElement createTenantButton;

    @FindBy(className = "n-input__input")
    private WebElement searchValInput;

    @FindBy(className = "n-button__content")
    private WebElement searchValButton;

    @FindBy(className = "items")
    private List<WebElement> tenantList;

    public TenantManagePage() {
    }

    public TenantManagePage create(String tenant, String description) {
        createTenantButton.click();
        tenantForm.create(tenant, description);
        return this;
    }

    public TenantManagePage create(TenantRequestEntity tenantRequestEntity) {
        createTenantButton.click();
        tenantForm.create(tenantRequestEntity.getTenantCode(), tenantRequestEntity.getDescription());
        return this;
    }

    @Override
    protected void onLoad(Page previousPage) {
        super.onLoad(previousPage);
        try {
            this.tenantForm = this.getBrowser().createPage(TenantForm.class);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public List<WebElement> getTenantList() {
        return tenantList;
    }
}

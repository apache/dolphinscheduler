package org.apache.dolphinscheduler.test.cases.api;

import com.devskiller.jfairy.Fairy;
import org.apache.dolphinscheduler.test.cases.common.AbstractApiTest;
import org.apache.dolphinscheduler.test.cases.common.AbstractTenantApiTest;
import org.apache.dolphinscheduler.test.endpoint.api.common.FormParam;
import org.apache.dolphinscheduler.test.endpoint.api.common.PageParamEntity;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.TenantEndPoints;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantResponseEntity;
import org.apache.dolphinscheduler.test.endpoint.utils.RestResponse;
import org.apache.dolphinscheduler.test.endpoint.utils.Result;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;


@DisplayName("Tenant API interface test")
public class TenantApiTest extends AbstractTenantApiTest {
    @Test
    @Order(1)
    @DisplayName("Test the correct Tenant information to log in to the system")
    public void testCreateTenant() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(fairy.person().getUsername());
        tenantRequestEntity.setQueueId(1);
        tenantRequestEntity.setDescription(fairy.person().getFullName());
        RestResponse<Result> result = tenantEndPoints.createTenant(tenantRequestEntity);
        tenantResponseEntity = result.getResponse().jsonPath().getObject(FormParam.DATA.getParam(), TenantResponseEntity.class);
    }

    @Test
    public void testUpdateTenant() {
        TenantRequestEntity tenantUpdateEntity = new TenantRequestEntity();
        tenantUpdateEntity.setId(tenantResponseEntity.getId());
        tenantUpdateEntity.setTenantCode(tenantResponseEntity.getTenantCode());
        tenantUpdateEntity.setQueueId(1);
        tenantUpdateEntity.setDescription(fairy.person().getMobileTelephoneNumber());
        tenantEndPoints.updateTenant(tenantUpdateEntity, tenantResponseEntity.getId()).isResponseSuccessful();
    }

    @Test
    public void testQueryTenantlistPaging() {
        PageParamEntity pageParamEntity = new PageParamEntity();
        pageParamEntity.setPageNo(1);
        pageParamEntity.setPageSize(10);
        pageParamEntity.setSearchVal("");
        tenantEndPoints.getTenants(pageParamEntity).isResponseSuccessful();
    }


    @Test
    public void testQueryTenantlistAll() {
        tenantEndPoints.getTenantsListAll().isResponseSuccessful();
    }

    @Test
    @DisplayName("Verify that the existing tenant returns code 0")
    public void testVerifyExistTenantCode() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(fairy.person().getUsername());
        tenantEndPoints.verifyTenantCode(tenantRequestEntity).isResponseSuccessful();
    }

    @Test
    @DisplayName("Verify that the non-existent tenant returns code 10009")
    public void testVerifyNotExistTenantCode() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(tenantResponseEntity.getTenantCode());
        tenantEndPoints.verifyTenantCode(tenantRequestEntity).getResponse().then().
                body(FormParam.CODE.getParam(), equalTo(10009));
    }


    @Test
    @DisplayName("delete exist tenant by tenant id")
    public void testDeleteExistTenantByCode() {
        tenantEndPoints.deleteTenantById( tenantResponseEntity.getId()).isResponseSuccessful();
    }

}

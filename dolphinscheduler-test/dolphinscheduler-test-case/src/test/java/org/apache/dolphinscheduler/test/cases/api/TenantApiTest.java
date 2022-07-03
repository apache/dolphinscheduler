package org.apache.dolphinscheduler.test.cases.api;

import com.devskiller.jfairy.Fairy;
import org.apache.dolphinscheduler.test.apis.EndPoints;
import org.apache.dolphinscheduler.test.apis.common.FormParam;
import org.apache.dolphinscheduler.test.apis.common.PageParamEntity;
import org.apache.dolphinscheduler.test.apis.configCenter.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.apis.configCenter.tenant.entity.TenantResponseEntity;
import org.apache.dolphinscheduler.test.base.AbstractControllerTest;
import org.apache.dolphinscheduler.test.utils.RestResponse;
import org.apache.dolphinscheduler.test.utils.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Tenant API interface test")
public class TenantApiTest extends AbstractControllerTest {
    private final Fairy fairy = Fairy.create();
    private TenantResponseEntity tenantResponseEntity = null;

    @Test
    @Order(1)
    @DisplayName("Test the correct Tenant information to log in to the system")
    public void testCreateTenant() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(fairy.person().getUsername());
        tenantRequestEntity.setQueueId(1);
        tenantRequestEntity.setDescription(fairy.person().getFullName());
        RestResponse<Result> result = EndPoints.createTenant(request, sessionId, tenantRequestEntity);
        tenantResponseEntity = result.getResponse().jsonPath().getObject(FormParam.DATA.getParam(), TenantResponseEntity.class);
    }

    @Test
    public void testUpdateTenant() {
        TenantRequestEntity tenantUpdateEntity = new TenantRequestEntity();
        tenantUpdateEntity.setId(tenantResponseEntity.getId());
        tenantUpdateEntity.setTenantCode(tenantResponseEntity.getTenantCode());
        tenantUpdateEntity.setQueueId(1);
        tenantUpdateEntity.setDescription(fairy.person().getMobileTelephoneNumber());
        EndPoints.updateTenant(given().spec(reqSpec), sessionId, tenantUpdateEntity, tenantResponseEntity.getId()).isResponseSuccessful();
    }

    @Test
    public void testQueryTenantlistPaging() {
        PageParamEntity pageParamEntity = new PageParamEntity();
        pageParamEntity.setPageNo(1);
        pageParamEntity.setPageSize(10);
        pageParamEntity.setSearchVal("");
        EndPoints.getTenants(request, sessionId, pageParamEntity).isResponseSuccessful();
    }


    @Test
    public void testQueryTenantlistAll() {
        EndPoints.getTenantsListAll(request, sessionId).isResponseSuccessful();
    }

    @Test
    @DisplayName("Verify that the existing tenant returns code 0")
    public void testVerifyExistTenantCode() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(fairy.person().getUsername());
        EndPoints.verifyTenantCode(request, sessionId, tenantRequestEntity).isResponseSuccessful();
    }

    @Test
    @DisplayName("Verify that the non-existent tenant returns code 1009")
    public void testVerifyNotExistTenantCode() {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(tenantResponseEntity.getTenantCode());
        EndPoints.verifyTenantCode(request, sessionId, tenantRequestEntity).getResponse().then().
                body(FormParam.CODE.getParam(), equalTo(10009));
    }


    @Test
    @DisplayName("delete exist tenant by tenant id")
    public void testDeleteExistTenantByCode() {
        EndPoints.deleteTenantById(request, sessionId, tenantResponseEntity.getId()).isResponseSuccessful();
    }

}

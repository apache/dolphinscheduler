package org.apache.dolphinscheduler.test.endpoint.api.security.tenant;

import org.apache.dolphinscheduler.test.endpoint.EndPoint;
import org.apache.dolphinscheduler.test.endpoint.api.common.PageParamEntity;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.endpoint.utils.RestResponse;
import org.apache.dolphinscheduler.test.endpoint.utils.Result;

public interface ITenantEndPoints extends EndPoint {
    RestResponse<Result> createTenant(TenantRequestEntity tenantRequestEntity);

    RestResponse<Result> updateTenant(TenantRequestEntity tenantUpdateEntity, int tenantId);

    RestResponse<Result> getTenants(PageParamEntity pageParamEntity);

    RestResponse<Result> getTenantsListAll();

    RestResponse<Result> verifyTenantCode(TenantRequestEntity tenantRequestEntity);

    RestResponse<Result> deleteTenantById(int tenantId);
}

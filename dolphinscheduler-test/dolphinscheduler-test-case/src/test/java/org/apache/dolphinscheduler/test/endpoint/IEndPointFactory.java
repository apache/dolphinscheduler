package org.apache.dolphinscheduler.test.endpoint;

import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.TenantEndPoints;

public interface IEndPointFactory {

    TenantEndPoints createTenantEndPoints();

}

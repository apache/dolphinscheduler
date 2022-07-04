package org.apache.dolphinscheduler.test.endpoint;

import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.TenantEndPoints;

public class EndPointFactory implements IEndPointFactory {
    private final RequestSpecification request;
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public EndPointFactory(RequestSpecification request, RequestSpecification reqSpec, String sessionId) {
        this.request = request;
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public TenantEndPoints createTenantEndPoints() {
        return new TenantEndPoints(request, reqSpec, sessionId);
    }


}

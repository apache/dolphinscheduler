package org.apache.dolphinscheduler.test.cases.common;

import com.devskiller.jfairy.Fairy;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.TenantEndPoints;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantResponseEntity;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractTenantApiTest extends AbstractApiTest {
    protected final Fairy fairy = Fairy.create();
    protected TenantResponseEntity tenantResponseEntity = null;
    protected TenantEndPoints tenantEndPoints = null;

    @BeforeAll
    public void initTenantEndPointFactory() {
        tenantEndPoints = endPointFactory.createTenantEndPoints();
    }

}

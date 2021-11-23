package org.apache.dolphinscheduler.service.cache;

import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.service.cache.impl.TenantCacheProxyImpl;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.annotation.EnableCaching;

/**
 * tenant cache proxy test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TenantCacheProxyTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private TenantCacheProxyImpl tenantCacheProxy;

    @Mock
    private TenantMapper tenantMapper;

    @Test
    public void testQueryById() {
        Tenant tenant = new Tenant();
        tenant.setId(100);

        Mockito.when(tenantMapper.queryById(100)).thenReturn(tenant);
        Assert.assertEquals(tenant, tenantCacheProxy.queryById(100));
    }
}

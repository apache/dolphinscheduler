package org.apache.dolphinscheduler.tools.demo;

import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateDemoTenant {

    private static final Logger logger = LoggerFactory.getLogger(CreateDemoTenant.class);
    @Autowired
    private TenantMapper tenantMapper;

    public void createTenantCode(String tenantCode){
        Date now = new Date();

        if( !tenantCode.equals("default")){
            Boolean existTenant = tenantMapper.existTenant(tenantCode);
            if( !Boolean.TRUE.equals(existTenant) ){
                Tenant tenant = new Tenant();
                tenant.setTenantCode(tenantCode);
                tenant.setQueueId(1);
                tenant.setDescription("");
                tenant.setCreateTime(now);
                tenant.setUpdateTime(now);
                // save
                tenantMapper.insert(tenant);
                logger.info("create tenant success");
            }else {
                logger.info("os tenant code already exists");
            }
        }
    }
}

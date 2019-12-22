/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class TenantServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(TenantServiceTest.class);

    @Autowired
    private TenantService tenantService;
    @Autowired
    private TenantMapper tenantMapper;

    private String tenantCode ="TenantServiceTest";
    private String tenantName ="TenantServiceTest";



    @Before
    public void setUp() {
        remove();
    }

    @After
    public void after(){
        remove();
    }

    @Test
    public void testCreateTenant(){

        User loginUser = getLoginUser();
        try {
            //check tenantCode
            Map<String, Object> result = tenantService.createTenant(getLoginUser(), "%!1111", tenantName, 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.VERIFY_TENANT_CODE_ERROR);

            // check add
            result = tenantService.createTenant(loginUser, tenantCode, tenantName, 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.SUCCESS);

            //check exist
            result = tenantService.createTenant(loginUser, tenantCode, tenantName, 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.REQUEST_PARAMS_NOT_VALID_ERROR);

        } catch (Exception e) {
          logger.error("create tenant error",e);
          Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryTenantListPage(){

        add();
        Map<String, Object> result = tenantService.queryTenantList(getLoginUser(), "TenantServiceTest", 1, 10);
        logger.info(result.toString());
        PageInfo<Tenant> pageInfo = (PageInfo<Tenant>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(pageInfo.getLists().size()>0);

    }

    @Test
    public void testUpdateTenant(){
        try {
            // id not exist
            Map<String, Object> result = tenantService.updateTenant(getLoginUser(), 912222, tenantCode, tenantName, 1, "desc");
            logger.info(result.toString());
            // add
            add();
            Assert.assertEquals(result.get(Constants.STATUS),Status.TENANT_NOT_EXIST);
            result = tenantService.updateTenant(getLoginUser(), getTenantId(), tenantCode, "TenantServiceTest001", 1, "desc");
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.SUCCESS);
            // get update tenant
            Tenant tenant = getTenant();
            //check update field
            Assert.assertEquals(tenant.getTenantName(),"TenantServiceTest001");

        } catch (Exception e) {
            logger.error("update tenant error",e);
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testDeleteTenantById(){

        try {
            Map<String, Object> result = tenantService.deleteTenantById(getLoginUser(), Integer.MAX_VALUE);
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.TENANT_NOT_EXIST);
            // add
            add();
            result = tenantService.deleteTenantById(getLoginUser(), getTenantId());
            logger.info(result.toString());
            Assert.assertEquals(result.get(Constants.STATUS),Status.SUCCESS);

            // get add tenant
            Tenant tenant = getTenant();
            //check exist
            Assert.assertNull(tenant);

        } catch (Exception e) {
            logger.error("delete tenant error",e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryTenantList(){
        add();
        Map<String, Object> result = tenantService.queryTenantList(getLoginUser());
        logger.info(result.toString());
        List<Tenant> tenantList = (List<Tenant>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(tenantList.size()>0);
    }

    @Test
    public void testVerifyTenantCode(){

        // tenantCode not exist
        Result result = tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf");
        logger.info(result.toString());
        Assert.assertEquals(result.getMsg(),Status.SUCCESS.getMsg());
        //add
        add();
        // tenantCode  exist
        result = tenantService.verifyTenantCode(getTenant().getTenantCode());
        logger.info(result.toString());
        Assert.assertEquals(result.getMsg(),Status.TENANT_NAME_EXIST.getMsg());
    }


    /**
     * add tenant
     */
    private void add(){

        try {
            tenantService.createTenant(getLoginUser(), tenantCode, tenantName, 1, "TenantServiceTest");
        } catch (Exception e) {
            logger.error("create tenant error",e);
        }

    }

    /**
     * get user
     * @return
     */
    private User getLoginUser(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    /**
     * get  add tenant
     * @return
     */
    private Tenant getTenant(){

        List<Tenant> tenantList = tenantMapper.queryByTenantCode(tenantCode);
        if (CollectionUtils.isNotEmpty(tenantList)){
           return tenantList.get(0);
        }
        return null;
    }

    /**
     * get  add tenant id
     * @return
     */
    private int getTenantId(){

        Tenant tenant = getTenant();
        if (tenant != null){
            return tenant.getId();
        }
        return 0;
    }

    /**
     * remove add tenant
     */
    private void remove(){

        List<Tenant> tenantList = tenantMapper.queryByTenantCode(tenantCode);
        if (CollectionUtils.isNotEmpty(tenantList)){
            for (Tenant tenant : tenantList) {
                tenantMapper.deleteById(tenant.getId());
            }
        }
    }
}
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RunWith(MockitoJUnitRunner.class)
public class TenantServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(TenantServiceTest.class);

    @InjectMocks
    private TenantService tenantService;
    @Mock
    private TenantMapper tenantMapper;
    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;
    @Mock
    private ProcessInstanceMapper processInstanceMapper;
    @Mock
    private UserMapper userMapper;

    private String tenantCode = "TenantServiceTest";
    private String tenantName = "TenantServiceTest";


    @Test
    public void testCreateTenant(){

        User loginUser = getLoginUser();
        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getList());
        try {
            //check tenantCode
            Map<String, Object> result = tenantService.createTenant(getLoginUser(), "%!1111", tenantName, 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(Status.VERIFY_TENANT_CODE_ERROR,result.get(Constants.STATUS));

            //check exist
            result = tenantService.createTenant(loginUser, tenantCode, tenantName, 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR,result.get(Constants.STATUS));

            // success
            result = tenantService.createTenant(loginUser, "test", "test", 1, "TenantServiceTest");
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
            
        } catch (Exception e) {
          logger.error("create tenant error",e);
          Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryTenantListPage(){

        IPage<Tenant> page = new Page<>(1,10);
        page.setRecords(getList());
        page.setTotal(1L);
        Mockito.when(tenantMapper.queryTenantPaging(Mockito.any(Page.class), Mockito.eq("TenantServiceTest"))).thenReturn(page);
        Map<String, Object> result = tenantService.queryTenantList(getLoginUser(), "TenantServiceTest", 1, 10);
        logger.info(result.toString());
        PageInfo<Tenant> pageInfo = (PageInfo<Tenant>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));

    }

    @Test
    public void testUpdateTenant(){

        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        try {
            // id not exist
            Map<String, Object> result = tenantService.updateTenant(getLoginUser(), 912222, tenantCode, tenantName, 1, "desc");
            logger.info(result.toString());
            // success
            Assert.assertEquals(Status.TENANT_NOT_EXIST,result.get(Constants.STATUS));
            result = tenantService.updateTenant(getLoginUser(), 1, tenantCode, "TenantServiceTest001", 1, "desc");
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        } catch (Exception e) {
            logger.error("update tenant error",e);
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testDeleteTenantById(){

        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        Mockito.when(processInstanceMapper.queryByTenantIdAndStatus(1, Constants.NOT_TERMINATED_STATES)).thenReturn(getInstanceList());
        Mockito.when(processDefinitionMapper.queryDefinitionListByTenant(2)).thenReturn(getDefinitionsList());
        Mockito.when( userMapper.queryUserListByTenant(3)).thenReturn(getUserList());

        try {
            //TENANT_NOT_EXIST
            Map<String, Object> result = tenantService.deleteTenantById(getLoginUser(),12);
            logger.info(result.toString());
            Assert.assertEquals(Status.TENANT_NOT_EXIST,result.get(Constants.STATUS));

            //DELETE_TENANT_BY_ID_FAIL
            result = tenantService.deleteTenantById(getLoginUser(),1);
            logger.info(result.toString());
            Assert.assertEquals(Status.DELETE_TENANT_BY_ID_FAIL,result.get(Constants.STATUS));

            //DELETE_TENANT_BY_ID_FAIL_DEFINES
            Mockito.when(tenantMapper.queryById(2)).thenReturn(getTenant(2));
            result = tenantService.deleteTenantById(getLoginUser(),2);
            logger.info(result.toString());
            Assert.assertEquals(Status.DELETE_TENANT_BY_ID_FAIL_DEFINES,result.get(Constants.STATUS));

            //DELETE_TENANT_BY_ID_FAIL_USERS
            Mockito.when(tenantMapper.queryById(3)).thenReturn(getTenant(3));
            result = tenantService.deleteTenantById(getLoginUser(),3);
            logger.info(result.toString());
            Assert.assertEquals(Status.DELETE_TENANT_BY_ID_FAIL_USERS,result.get(Constants.STATUS));

            // success
            Mockito.when(tenantMapper.queryById(4)).thenReturn(getTenant(4));
            result = tenantService.deleteTenantById(getLoginUser(),4);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        } catch (Exception e) {
            logger.error("delete tenant error",e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryTenantList(){

        Mockito.when( tenantMapper.selectList(null)).thenReturn(getList());
        Map<String, Object> result = tenantService.queryTenantList(getLoginUser());
        logger.info(result.toString());
        List<Tenant> tenantList = (List<Tenant>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(tenantList));

        Mockito.when( tenantMapper.queryByTenantCode("1")).thenReturn(getList());
        Map<String, Object> successRes = tenantService.queryTenantList("1");
        Assert.assertEquals(Status.SUCCESS,successRes.get(Constants.STATUS));

        Mockito.when( tenantMapper.queryByTenantCode("1")).thenReturn(null);
        Map<String, Object> tenantNotExistRes = tenantService.queryTenantList("1");
        Assert.assertEquals(Status.TENANT_NOT_EXIST,tenantNotExistRes.get(Constants.STATUS));
    }

    @Test
    public void testVerifyTenantCode(){

        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getList());
        // tenantCode not exist
        Result result = tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // tenantCode  exist
        result = tenantService.verifyTenantCode(getTenant().getTenantCode());
        String resultString;
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            resultString = "租户编码[TenantServiceTest]已存在";
        } else {
            resultString = "tenant code TenantServiceTest already exists";
        }
        logger.info(result.toString());
        Assert.assertEquals(resultString, result.getMsg());
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
     * get  list
     * @return
     */
    private List<Tenant> getList(){
        List<Tenant> tenantList = new ArrayList<>();
        tenantList.add(getTenant());
        return tenantList;
    }

    /**
     * get   tenant
     * @return
     */
    private Tenant getTenant(){
        return getTenant(1);
    }
    /**
     * get   tenant
     * @return
     */
    private Tenant getTenant(int id){
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(tenantName);
        return tenant;
    }

    private List<User> getUserList(){
        List<User> userList = new ArrayList<>();
        userList.add(getLoginUser());
        return userList;
    }

    private List<ProcessInstance> getInstanceList(){
        List<ProcessInstance> processInstances = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstances.add(processInstance);
        return processInstances;
    }

    private List<ProcessDefinition>  getDefinitionsList(){
        List<ProcessDefinition> processDefinitions = new ArrayList<>();
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinitions.add(processDefinition);
        return processDefinitions;
    }


}

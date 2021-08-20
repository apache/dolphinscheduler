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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.EnvironmentServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * environment service test
 */
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentServiceTest {

    public static final Logger logger = LoggerFactory.getLogger(EnvironmentServiceTest.class);

    @InjectMocks
    private EnvironmentServiceImpl environmentService;

    @Mock
    private EnvironmentMapper environmentMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    public static final String testUserName = "environmentServerTest";

    public static final String environmentName = "Env1";

    @Before
    public void setUp(){
    }

    @After
    public void after(){
    }

    @Test
    public void testCreateEnvironment() {
        User loginUser = getGeneralUser();
        Map<String, Object> result = environmentService.createEnvironment(loginUser,environmentName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        result = environmentService.createEnvironment(loginUser,environmentName,"",getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_CONFIG_IS_NULL, result.get(Constants.STATUS));

        result = environmentService.createEnvironment(loginUser,"",getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_NAME_IS_NULL, result.get(Constants.STATUS));

        Mockito.when(environmentMapper.queryByEnvironmentName(environmentName)).thenReturn(getEnvironment());
        result = environmentService.createEnvironment(loginUser,environmentName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_NAME_EXISTS, result.get(Constants.STATUS));

        Mockito.when(environmentMapper.insert(Mockito.any(Environment.class))).thenReturn(1);
        result = environmentService.createEnvironment(loginUser,"testName","test","test");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testUpdateEnvironmentByCode() {
        User loginUser = getGeneralUser();
        Map<String, Object> result = environmentService.updateEnvironmentByCode(loginUser,1L,environmentName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        result = environmentService.updateEnvironmentByCode(loginUser,1L,environmentName,"",getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_CONFIG_IS_NULL, result.get(Constants.STATUS));

        result = environmentService.updateEnvironmentByCode(loginUser,1L,"",getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_NAME_IS_NULL, result.get(Constants.STATUS));

        Mockito.when(environmentMapper.queryByEnvironmentName(environmentName)).thenReturn(getEnvironment());
        result = environmentService.updateEnvironmentByCode(loginUser,2L,environmentName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.ENVIRONMENT_NAME_EXISTS, result.get(Constants.STATUS));

        Mockito.when(environmentMapper.update(Mockito.any(Environment.class),Mockito.any(Wrapper.class))).thenReturn(1);
        result = environmentService.updateEnvironmentByCode(loginUser,1L,"testName","test","test");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testQueryAllEnvironmentList() {
        Mockito.when(environmentMapper.queryAllEnvironmentList()).thenReturn(Lists.newArrayList(getEnvironment()));
        Map<String, Object> result  = environmentService.queryAllEnvironmentList();
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

        List<Environment> list = (List<Environment>)(result.get(Constants.DATA_LIST));
        Assert.assertEquals(1,list.size());
    }

    @Test
    public void testQueryEnvironmentByName() {
        Mockito.when(environmentMapper.queryByEnvironmentName(environmentName)).thenReturn(null);
        Map<String, Object> result = environmentService.queryEnvironmentByName(environmentName);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUERY_ENVIRONMENT_BY_NAME_ERROR,result.get(Constants.STATUS));

        Mockito.when(environmentMapper.queryByEnvironmentName(environmentName)).thenReturn(getEnvironment());
        result = environmentService.queryEnvironmentByName(environmentName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public void testQueryEnvironmentByCode() {
        Mockito.when(environmentMapper.queryByEnvironmentCode(1L)).thenReturn(null);
        Map<String, Object> result = environmentService.queryEnvironmentByCode(1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUERY_ENVIRONMENT_BY_CODE_ERROR,result.get(Constants.STATUS));

        Mockito.when(environmentMapper.queryByEnvironmentCode(1L)).thenReturn(getEnvironment());
        result = environmentService.queryEnvironmentByCode(1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteEnvironmentByCode() {
        User loginUser = getGeneralUser();
        Map<String, Object> result = environmentService.deleteEnvironmentByCode(loginUser,1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        Mockito.when(taskDefinitionMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1);
        result = environmentService.deleteEnvironmentByCode(loginUser,1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.DELETE_ENVIRONMENT_RELATED_TASK_EXISTS, result.get(Constants.STATUS));

        Mockito.when(taskDefinitionMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(0);
        Mockito.when(environmentMapper.deleteByCode(1L)).thenReturn(1);
        result = environmentService.deleteEnvironmentByCode(loginUser,1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    private Environment getEnvironment() {
        Environment environment = new Environment();
        environment.setId(1);
        environment.setCode(1L);
        environment.setName(environmentName);
        environment.setConfig(getConfig());
        environment.setDescription(getDesc());
        environment.setOperator(1);
        return environment;
    }

    /**
     * create an environment description
     */
    private String getDesc() {
        return "create an environment to test ";
    }

    /**
     * create an environment config
     */
    private String getConfig() {
        return "export HADOOP_HOME=/opt/hadoop-2.6.5\n"
                + "export HADOOP_CONF_DIR=/etc/hadoop/conf\n"
                + "export SPARK_HOME1=/opt/soft/spark1\n"
                + "export SPARK_HOME2=/opt/soft/spark2\n"
                + "export PYTHON_HOME=/opt/soft/python\n"
                + "export JAVA_HOME=/opt/java/jdk1.8.0_181-amd64\n"
                + "export HIVE_HOME=/opt/soft/hive\n"
                + "export FLINK_HOME=/opt/soft/flink\n"
                + "export DATAX_HOME=/opt/soft/datax\n"
                + "export YARN_CONF_DIR=\"/etc/hadoop/conf\"\n"
                + "\n"
                + "export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH\n"
                + "\n"
                + "export HADOOP_CLASSPATH=`hadoop classpath`\n"
                + "\n"
                + "#echo \"HADOOP_CLASSPATH=\"$HADOOP_CLASSPATH";
    }

    /**
     * create general user
     */
    private User getGeneralUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(testUserName);
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * create admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(testUserName);
        loginUser.setId(1);
        return loginUser;
    }
}

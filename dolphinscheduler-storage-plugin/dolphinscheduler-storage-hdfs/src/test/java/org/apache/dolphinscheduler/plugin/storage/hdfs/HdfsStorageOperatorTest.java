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

package org.apache.dolphinscheduler.plugin.storage.hdfs;

import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hadoop utils test
 */
@ExtendWith(MockitoExtension.class)
public class HdfsStorageOperatorTest {

    private static final Logger logger = LoggerFactory.getLogger(HdfsStorageOperatorTest.class);

    @Test
    void getHdfsTenantDir() {
        HdfsStorageOperator hdfsStorageOperator = new HdfsStorageOperator();
        logger.info(hdfsStorageOperator.getHdfsTenantDir("1234"));
        Assertions.assertTrue(true);
    }

    @Test
    void getHdfsUdfFileName() {
        HdfsStorageOperator hdfsStorageOperator = new HdfsStorageOperator();
        logger.info(hdfsStorageOperator.getHdfsUdfFileName("admin", "file_name"));
        Assertions.assertTrue(true);
    }

    @Test
    void getHdfsResourceFileName() {
        HdfsStorageOperator hdfsStorageOperator = new HdfsStorageOperator();
        logger.info(hdfsStorageOperator.getHdfsResourceFileName("admin", "file_name"));
        Assertions.assertTrue(true);
    }

    @Test
    void getHdfsFileName() {
        HdfsStorageOperator hdfsStorageOperator = new HdfsStorageOperator();
        logger.info(hdfsStorageOperator.getHdfsFileName(ResourceType.FILE, "admin", "file_name"));
        Assertions.assertTrue(true);
    }

    @Test
    void getAppAddress() {
        HdfsStorageOperator hdfsStorageOperator = new HdfsStorageOperator();
        try (MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.get("http://ds1:8088/ws/v1/cluster/info"))
                    .thenReturn("{\"clusterInfo\":{\"state\":\"STARTED\",\"haState\":\"ACTIVE\"}}");
            logger.info(hdfsStorageOperator.getAppAddress("http://ds1:8088/ws/v1/cluster/apps/%s", "ds1,ds2"));
            Assertions.assertTrue(true);
        }
    }

    @DisplayName("test load Hdfs Configuration by env avaliable HADOOP_CONF_DIR, and directory exist")
    @Test
    void testGetHadoopConfPathFromEnvByHADOOP_CONF_DIR1() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String hadoopConfDirEnv = System.getProperty("user.dir");
        String hadoopHomeEnv = "/not_expected";
        Assertions.assertEquals(hadoopConfDirEnv, invokeGetHadoopConfPath(hadoopConfDirEnv, hadoopHomeEnv));
    }

    @DisplayName("test load Hdfs Configuration by env avaliable HADOOP_CONF_DIR, but directory not exist")
    @Test
    void testGetHadoopConfPathFromEnvByHADOOP_CONF_DIR2() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String hadoopConfDirEnv = "/not_exist";
        String hadoopHomeEnv = null;
        Assertions.assertNull(invokeGetHadoopConfPath(hadoopConfDirEnv, hadoopHomeEnv));
    }

    @DisplayName("test load Hdfs Configuration by env avaliable HADOOP_HOME, and directory exist")
    @Test
    void testGetHadoopConfPathFromEnvByHADOOP_HOME1() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        String hadoopConfDirEnv = null;
        String hadoopHomeEnv = System.getProperty("user.dir");
        Path hoemConfPath = Paths.get(hadoopHomeEnv, "conf");
        Files.createDirectory(hoemConfPath);
        Assertions.assertEquals(hoemConfPath.toString(),
                invokeGetHadoopConfPath(hadoopConfDirEnv, hadoopHomeEnv));
        Files.delete(hoemConfPath);
    }

    @DisplayName("test load Hdfs Configuration by env avaliable HADOOP_HOME, and directory not exist")
    @Test
    void testGetHadoopConfPathFromEnvByHADOOP_HOME2() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String hadoopConfDirEnv = null;
        String hadoopHomeEnv = "/not_exist";
        Assertions.assertNull(invokeGetHadoopConfPath(hadoopConfDirEnv, hadoopHomeEnv));
    }

    private String invokeGetHadoopConfPath(String hadoopConfDirEnv,
                                           String hadoopHomeEnv) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method =
                HdfsStorageOperator.class.getDeclaredMethod("getHadoopConfPath", String.class, String.class);
        method.setAccessible(true);
        HdfsStorageOperator hdfsStorageOperator = Mockito.mock(HdfsStorageOperator.class);
        return (String) method.invoke(hdfsStorageOperator, hadoopConfDirEnv, hadoopHomeEnv);
    }

}

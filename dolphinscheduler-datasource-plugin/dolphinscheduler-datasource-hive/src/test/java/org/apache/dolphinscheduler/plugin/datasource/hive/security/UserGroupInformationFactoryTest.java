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

package org.apache.dolphinscheduler.plugin.datasource.hive.security;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.hadoop.security.UserGroupInformation;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserGroupInformationFactoryTest {

    private MockedStatic<PropertyUtils> mockedPropertyUtils;

    @BeforeEach
    void setUp() throws Exception {
        mockedPropertyUtils = Mockito.mockStatic(PropertyUtils.class);
        mockedPropertyUtils
                .when(() -> PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE))
                .thenReturn("LOCAL");
        clearInternalMaps();
    }

    @AfterEach
    void tearDown() {
        mockedPropertyUtils.close();
    }

    @Test
    void testForEachConcurrentWithModification() throws Exception {
        for (int i = 0; i < 10; i++) {
            UserGroupInformationFactory.login("user_" + i);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger errorCount = new AtomicInteger(0);

        Thread iteratorThread = new Thread(() -> {
            try {
                startLatch.await();
                for (int round = 0; round < 10000; round++) {
                    getUserGroupInformationMap().forEach((key, ugi) -> {
                    });
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });
        iteratorThread.start();

        Thread modifierPool = new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 0; i < 500; i++) {
                    for (int j = 0; j < 10; j++) {
                        String user = "user_" + j;
                        UserGroupInformationFactory.logout(user);
                        UserGroupInformationFactory.login(user);
                    }
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });
        modifierPool.start();

        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        Assertions.assertTrue(completed, "Test should complete within timeout");
        Assertions.assertEquals(0, errorCount.get(),
                "No ConcurrentModificationException should occur during concurrent forEach and modification");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, UserGroupInformation> getUserGroupInformationMap() throws Exception {
        Field field = UserGroupInformationFactory.class.getDeclaredField("userGroupInformationMap");
        field.setAccessible(true);
        return (Map<String, UserGroupInformation>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static void clearInternalMaps() throws Exception {
        Field userGroupInfoMapField = UserGroupInformationFactory.class.getDeclaredField("userGroupInformationMap");
        userGroupInfoMapField.setAccessible(true);
        ((Map<String, ?>) userGroupInfoMapField.get(null)).clear();

        Field currentLoginTimesMapField = UserGroupInformationFactory.class.getDeclaredField("currentLoginTimesMap");
        currentLoginTimesMapField.setAccessible(true);
        ((Map<String, ?>) currentLoginTimesMapField.get(null)).clear();
    }
}

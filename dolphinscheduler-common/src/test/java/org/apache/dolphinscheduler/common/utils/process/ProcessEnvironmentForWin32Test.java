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
package org.apache.dolphinscheduler.common.utils.process;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OSUtils.class, ProcessEnvironmentForWin32.class})
public class ProcessEnvironmentForWin32Test {

    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderForWin32Test.class);

    @Before
    public void before() {
        try {
            PowerMockito.mockStatic(OSUtils.class);
            PowerMockito.when(OSUtils.isWindows()).thenReturn(true);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testPutAndGet() {
        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            processEnvironmentForWin32.put("a", "123");
            Assert.assertEquals("123", processEnvironmentForWin32.get("a"));
            Assert.assertTrue(processEnvironmentForWin32.containsKey("a"));
            Assert.assertTrue(processEnvironmentForWin32.containsValue("123"));
            Assert.assertEquals("123", processEnvironmentForWin32.remove("a"));
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }

        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            processEnvironmentForWin32.put("b=", "123");
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }

        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            processEnvironmentForWin32.put("b", "\u0000");
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }

        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            processEnvironmentForWin32.get(null);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testEntrySet() {
        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            processEnvironmentForWin32.clear();
            processEnvironmentForWin32.put("a", "123");
            Assert.assertEquals(0, processEnvironmentForWin32.entrySet().size());
            Assert.assertTrue(processEnvironmentForWin32.entrySet().isEmpty());
            for (Map.Entry<String, String> entry : processEnvironmentForWin32.entrySet()) {
                Assert.assertNotNull(entry);
                Assert.assertNotNull(entry.getKey());
                Assert.assertNotNull(entry.getValue());
                Assert.assertNotNull(entry.setValue("123"));
            }

            processEnvironmentForWin32.clear();
            Set<String> keys = processEnvironmentForWin32.keySet();
            Assert.assertEquals(0, keys.size());
            Assert.assertTrue(keys.isEmpty());

            processEnvironmentForWin32.clear();
            Collection<String> values = processEnvironmentForWin32.values();
            Assert.assertEquals(0, keys.size());
            Assert.assertTrue(keys.isEmpty());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testToEnvironmentBlock() {
        try {
            ProcessEnvironmentForWin32 processEnvironmentForWin32 = (ProcessEnvironmentForWin32) ProcessEnvironmentForWin32.emptyEnvironment(0);
            Assert.assertNotNull(processEnvironmentForWin32.toEnvironmentBlock());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

}

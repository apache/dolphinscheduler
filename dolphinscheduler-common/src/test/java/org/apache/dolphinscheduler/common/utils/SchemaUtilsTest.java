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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerFactory.class, FileUtils.class })
public class SchemaUtilsTest {

    @Test
    public void testReplaceBlank() {
        Assert.assertEquals("abc", SchemaUtils.replaceBlank(" abc"));
        Assert.assertEquals("abc", SchemaUtils.replaceBlank("abc "));
        Assert.assertEquals("abc", SchemaUtils.replaceBlank("a b c"));
        Assert.assertEquals("abc", SchemaUtils.replaceBlank("a b   c"));
        Assert.assertEquals("", SchemaUtils.replaceBlank("  "));
        Assert.assertEquals("", SchemaUtils.replaceBlank(null));
        Assert.assertEquals("我怕的你", SchemaUtils.replaceBlank("我怕的   你"));
    }

    @Test
    public void testGetSoftVersion() {
        // file not found
        try {
            SchemaUtils.getSoftVersion();
        } catch (RuntimeException e) {
            Assert.assertEquals("Failed to get the product version description file. The file could not be found",
                    e.getMessage());
        }

        // file exists, fmt is invalid
        FileUtils.writeContent2File("32432423", "sql/soft_version");
        Assert.assertEquals("32432423", SchemaUtils.getSoftVersion());
    }

    @Test
    public void testIsAGreatVersion() {
        // param is null
        try {
            SchemaUtils.isAGreatVersion(null, null);
        } catch (RuntimeException e) {
            Assert.assertEquals("schemaVersion or version is empty", e.getMessage());
        }

        // param is ""
        try {
            SchemaUtils.isAGreatVersion("", "");
        } catch (RuntimeException e) {
            Assert.assertEquals("schemaVersion or version is empty", e.getMessage());
        }
        Assert.assertFalse(SchemaUtils.isAGreatVersion("1", "1"));
        Assert.assertTrue(SchemaUtils.isAGreatVersion("2", "1"));
        Assert.assertTrue(SchemaUtils.isAGreatVersion("1.1", "1"));
        Assert.assertTrue(SchemaUtils.isAGreatVersion("1.1", "1.0.1"));
        Assert.assertFalse(SchemaUtils.isAGreatVersion("1.1", "1.2"));
        Assert.assertTrue(SchemaUtils.isAGreatVersion("1.1.1", "1.1"));
        Assert.assertTrue(SchemaUtils.isAGreatVersion("10.1.1", "1.01.100"));
        try {
            SchemaUtils.isAGreatVersion("10.1.1", ".1");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
        try {
            SchemaUtils.isAGreatVersion("a.1.1", "b.1");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetAllSchemaList() {
        //normal
        PowerMockito.mockStatic(FileUtils.class);
        File[] files = new File[4];
        files[0] = new File("sql/upgrade/1.2.0_schema");
        files[1] = new File("sql/upgrade/1.0.1_schema");
        files[2] = new File("sql/upgrade/1.0.2_schema");
        files[3] = new File("sql/upgrade/1.1.0_schema");
        PowerMockito.when(FileUtils.getAllDir("sql/upgrade")).thenReturn(files);
        List<String> real = SchemaUtils.getAllSchemaList();
        List<String> expect = Arrays.asList("1.0.1_schema", "1.0.2_schema",
                "1.1.0_schema", "1.2.0_schema");
        Assert.assertTrue(CollectionUtils.isEqualCollection(real, expect));

        //normal
        files = new File[0];
        PowerMockito.when(FileUtils.getAllDir("sql/upgrade")).thenReturn(files);
        real = SchemaUtils.getAllSchemaList();
        Assert.assertNull(real);
    }
}

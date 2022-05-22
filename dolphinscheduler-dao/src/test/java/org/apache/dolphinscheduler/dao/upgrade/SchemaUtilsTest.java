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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SchemaUtilsTest {

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
            Assert.fail("Should fail");
        } catch (Exception ignored) {
            // This is expected
        }
        try {
            SchemaUtils.isAGreatVersion("a.1.1", "b.1");
            Assert.fail("Should fail");
        } catch (Exception ignored) {
            // This is expected
        }
    }

    @Test
    public void testGetAllSchemaList() {
        List<String> list = null;
        try {
            list = SchemaUtils.getAllSchemaList();
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertFalse("Can not find any schema files", CollectionUtils.isEmpty(list));
    }
}

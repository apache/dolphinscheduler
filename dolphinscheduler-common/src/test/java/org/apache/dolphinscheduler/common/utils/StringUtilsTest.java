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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testIsNotEmpty() {
        //null string
        boolean b = StringUtils.isNotEmpty(null);
        Assert.assertFalse(b);

        //"" string
        b = StringUtils.isNotEmpty("");
        Assert.assertFalse(b);

        //" " string
        b = StringUtils.isNotEmpty(" ");
        Assert.assertTrue(b);

        //"test" string
        b = StringUtils.isNotEmpty("test");
        Assert.assertTrue(b);
    }

    @Test
    public void testIsNotBlank() {
        //null string
        boolean b = StringUtils.isNotBlank(null);
        Assert.assertFalse(b);

        //"" string
        b = StringUtils.isNotBlank("");
        Assert.assertFalse(b);

        //" " string
        b = StringUtils.isNotBlank(" ");
        Assert.assertFalse(b);

        //" test " string
        b = StringUtils.isNotBlank(" test ");
        Assert.assertTrue(b);

        //"test" string
        b = StringUtils.isNotBlank("test");
        Assert.assertTrue(b);
    }

    @Test
    public void testTrim() {
        String trim = StringUtils.trim(null);
        Assert.assertNull(trim);

        trim = StringUtils.trim(" test ");
        Assert.assertEquals("test", trim);
    }

    @Test
    public void testDefaultIfBlank() {
        String defaultStr = StringUtils.defaultIfBlank("", "defaultStr");
        Assert.assertEquals("defaultStr", defaultStr);

        defaultStr = StringUtils.defaultIfBlank("test", "defaultStr");
        Assert.assertEquals("test", defaultStr);
    }

    @Test
    public void testJoin() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("3");
        list.add("4");
        String join = StringUtils.join(list,  ",");
        Assert.assertEquals("1,3,4", join);
    }
}

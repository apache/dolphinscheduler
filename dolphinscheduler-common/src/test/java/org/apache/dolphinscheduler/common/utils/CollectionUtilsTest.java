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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CollectionUtilsTest {

    @Test
    public void equalLists() {
        List<Integer> a = new ArrayList<Integer>();
        a.add(1);
        a.add(2);
        a.add(3);
        List<Integer> b = new ArrayList<Integer>();
        b.add(3);
        b.add(2);
        b.add(1);
        Assert.assertTrue(CollectionUtils.equalLists(a,b));
        Assert.assertTrue(CollectionUtils.equalLists(null,null));
        List<Integer> c = new ArrayList<Integer>();
        Assert.assertFalse(CollectionUtils.equalLists(c,null));
        Assert.assertFalse(CollectionUtils.equalLists(c,a));
    }

    @Test
    public void subtract() {
        Set<Integer> a = new HashSet<Integer>();
        a.add(1);
        a.add(2);
        a.add(3);
        Set<Integer> b = new HashSet<Integer>();
        b.add(0);
        b.add(2);
        b.add(4);
        Assert.assertArrayEquals(new Integer[]{1,3},CollectionUtils.subtract(a,b).toArray());
    }
}

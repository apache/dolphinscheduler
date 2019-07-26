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
package cn.escheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    @Test
    public void testIsNotEmpty() {
        Assert.assertFalse(CollectionUtils.isNotEmpty(null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(new ArrayList<>()));

        Assert.assertTrue(CollectionUtils.isNotEmpty(
                new ArrayList<>(Arrays.asList("foo", "bar"))));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(CollectionUtils.isEmpty(null));
        Assert.assertTrue(CollectionUtils.isEmpty(new ArrayList<>()));

        Assert.assertFalse(CollectionUtils.isEmpty(
                new ArrayList<>(Arrays.asList("foo", "bar"))));
    }

    @Test
    public void testStringToMap() {
        Assert.assertNull(CollectionUtils.stringToMap("", ","));
        Assert.assertNull(CollectionUtils.stringToMap("", ",", "bar"));
        Assert.assertNull(CollectionUtils.stringToMap(null, ",", "bar"));
        Assert.assertNull(CollectionUtils.stringToMap("foo", "", "bar"));
        Assert.assertNull(CollectionUtils.stringToMap("foo", null, "bar"));
    }

    @Test
    public void testEqualLists() {
        Assert.assertTrue(CollectionUtils.equalLists(null, null));
        Assert.assertTrue(CollectionUtils.equalLists(
                new ArrayList<>(), new ArrayList<>()));
        Assert.assertTrue(CollectionUtils.equalLists(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("foo", "123"))));

        Assert.assertFalse(CollectionUtils.equalLists(
                null, new ArrayList<>()));
        Assert.assertFalse(CollectionUtils.equalLists(
                new ArrayList<>(), null));
        Assert.assertFalse(CollectionUtils.equalLists(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("bar"))));
        Assert.assertFalse(CollectionUtils.equalLists(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("bar", "123"))));
    }

    @Test
    public void testIsEqualCollection() {
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                new ArrayList<>(), new ArrayList<>()));
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("foo", "123"))));

        Assert.assertFalse(CollectionUtils.isEqualCollection(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("bar"))));
        Assert.assertFalse(CollectionUtils.isEqualCollection(
                new ArrayList<>(Arrays.asList("foo", "123")),
                new ArrayList<>(Arrays.asList("bar", "123"))));
    }

    @Test
    public void testGetCardinalityMap() {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(null, 2);
        hashMap.put(-2147483648, 1);

        Assert.assertEquals(new HashMap<>(),
                CollectionUtils.getCardinalityMap(new ArrayList<>()));
        Assert.assertEquals(hashMap, CollectionUtils.getCardinalityMap(
                new ArrayList<>(Arrays.asList(null, -2147483648, null))));
    }

    @Test
    public void testGetListByExclusion() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("empty", false);

        Assert.assertEquals(new ArrayList<>(Arrays.asList(hashMap)),
                CollectionUtils.getListByExclusion(
                        new ArrayList<>(Arrays.asList("foo")),
                        new HashSet<>(Arrays.asList("bytes", "class"))));
    }
}
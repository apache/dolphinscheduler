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

package org.apache.dolphinscheduler.plugin.task.api.model;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

<<<<<<< HEAD
import org.junit.Assert;
import org.junit.Test;
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
>>>>>>> refs/remotes/origin/3.1.1-release

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONUtilsTest {

    @Test
    public void createArrayNodeTest() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str =
                "[{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"},{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}]";
        JsonNode jsonNode = JSONUtils.toJsonNode(property);

        ArrayNode arrayNode = JSONUtils.createArrayNode();
        ArrayList<JsonNode> objects = new ArrayList<>();
        objects.add(jsonNode);
        objects.add(jsonNode);

        ArrayNode jsonNodes = arrayNode.addAll(objects);
        String s = JSONUtils.toJsonString(jsonNodes);
<<<<<<< HEAD
        Assert.assertEquals(s, str);
=======
        Assertions.assertEquals(s, str);
>>>>>>> refs/remotes/origin/3.1.1-release

    }

    @Test
    public void toJsonNodeTest() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str = "{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}";

        JsonNode jsonNodes = JSONUtils.toJsonNode(property);
        String s = JSONUtils.toJsonString(jsonNodes);
<<<<<<< HEAD
        Assert.assertEquals(s, str);
=======
        Assertions.assertEquals(s, str);
>>>>>>> refs/remotes/origin/3.1.1-release

    }

    @Test
    public void createObjectNodeTest() {
        String jsonStr = "{\"a\":\"b\",\"b\":\"d\"}";

        ObjectNode objectNode = JSONUtils.createObjectNode();
        objectNode.put("a", "b");
        objectNode.put("b", "d");
        String s = JSONUtils.toJsonString(objectNode);
<<<<<<< HEAD
        Assert.assertEquals(s, jsonStr);
=======
        Assertions.assertEquals(s, jsonStr);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void toMap() {

        String jsonStr = "{\"id\":\"1001\",\"name\":\"Jobs\"}";

        Map<String, String> models = JSONUtils.toMap(jsonStr);
<<<<<<< HEAD
        Assert.assertEquals("1001", models.get("id"));
        Assert.assertEquals("Jobs", models.get("name"));
=======
        Assertions.assertEquals("1001", models.get("id"));
        Assertions.assertEquals("Jobs", models.get("name"));
>>>>>>> refs/remotes/origin/3.1.1-release

    }

    @Test
    public void convert2Property() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str = "{\"direct\":\"IN\",\"prop\":\"ds\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}";
        Property property1 = JSONUtils.parseObject(str, Property.class);
        Direct direct = property1.getDirect();
<<<<<<< HEAD
        Assert.assertEquals(Direct.IN, direct);
=======
        Assertions.assertEquals(Direct.IN, direct);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void string2MapTest() {
        String str = list2String();

        List<LinkedHashMap> maps = JSONUtils.toList(str,
                LinkedHashMap.class);

<<<<<<< HEAD
        Assert.assertEquals(1, maps.size());
        Assert.assertEquals("mysql200", maps.get(0).get("mysql service name"));
        Assert.assertEquals("192.168.xx.xx", maps.get(0).get("mysql address"));
        Assert.assertEquals("3306", maps.get(0).get("port"));
        Assert.assertEquals("80", maps.get(0).get("no index of number"));
        Assert.assertEquals("190", maps.get(0).get("database client connections"));
=======
        Assertions.assertEquals(1, maps.size());
        Assertions.assertEquals("mysql200", maps.get(0).get("mysql service name"));
        Assertions.assertEquals("192.168.xx.xx", maps.get(0).get("mysql address"));
        Assertions.assertEquals("3306", maps.get(0).get("port"));
        Assertions.assertEquals("80", maps.get(0).get("no index of number"));
        Assertions.assertEquals("190", maps.get(0).get("database client connections"));
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    public String list2String() {

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
        map1.put("mysql service name", "mysql200");
        map1.put("mysql address", "192.168.xx.xx");
        map1.put("port", "3306");
        map1.put("no index of number", "80");
        map1.put("database client connections", "190");

        List<LinkedHashMap<String, String>> maps = new ArrayList<>();
        maps.add(0, map1);
        String resultJson = JSONUtils.toJsonString(maps);
        return resultJson;
    }

    @Test
    public void testParseObject() {
<<<<<<< HEAD
        Assert.assertNull(JSONUtils.parseObject(""));
        Assert.assertNull(JSONUtils.parseObject("foo", String.class));
=======
        Assertions.assertNull(JSONUtils.parseObject(""));
        Assertions.assertNull(JSONUtils.parseObject("foo", String.class));
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testNodeString() {
<<<<<<< HEAD
        Assert.assertEquals("", JSONUtils.getNodeString("", "key"));
        Assert.assertEquals("", JSONUtils.getNodeString("abc", "key"));
        Assert.assertEquals("", JSONUtils.getNodeString("{\"bar\":\"foo\"}", "key"));
        Assert.assertEquals("foo", JSONUtils.getNodeString("{\"bar\":\"foo\"}", "bar"));
        Assert.assertEquals("[1,2,3]", JSONUtils.getNodeString("{\"bar\": [1,2,3]}", "bar"));
        Assert.assertEquals("{\"1\":\"2\",\"2\":3}",
=======
        Assertions.assertEquals("", JSONUtils.getNodeString("", "key"));
        Assertions.assertEquals("", JSONUtils.getNodeString("abc", "key"));
        Assertions.assertEquals("", JSONUtils.getNodeString("{\"bar\":\"foo\"}", "key"));
        Assertions.assertEquals("foo", JSONUtils.getNodeString("{\"bar\":\"foo\"}", "bar"));
        Assertions.assertEquals("[1,2,3]", JSONUtils.getNodeString("{\"bar\": [1,2,3]}", "bar"));
        Assertions.assertEquals("{\"1\":\"2\",\"2\":3}",
>>>>>>> refs/remotes/origin/3.1.1-release
                JSONUtils.getNodeString("{\"bar\": {\"1\":\"2\",\"2\":3}}", "bar"));
    }

    @Test
    public void testJsonByteArray() {
        String str = "foo";
        byte[] serializeByte = JSONUtils.toJsonByteArray(str);
        String deserialize = JSONUtils.parseObject(serializeByte, String.class);
<<<<<<< HEAD
        Assert.assertEquals(str, deserialize);
        str = null;
        serializeByte = JSONUtils.toJsonByteArray(str);
        deserialize = JSONUtils.parseObject(serializeByte, String.class);
        Assert.assertNull(deserialize);
=======
        Assertions.assertEquals(str, deserialize);
        str = null;
        serializeByte = JSONUtils.toJsonByteArray(str);
        deserialize = JSONUtils.parseObject(serializeByte, String.class);
        Assertions.assertNull(deserialize);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testToList() {
<<<<<<< HEAD
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("A1B2C3", null));
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("", null));
=======
        Assertions.assertEquals(new ArrayList(), JSONUtils.toList("A1B2C3", null));
        Assertions.assertEquals(new ArrayList(), JSONUtils.toList("", null));
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testCheckJsonValid() {
<<<<<<< HEAD
        Assert.assertTrue(JSONUtils.checkJsonValid("3"));
        Assert.assertFalse(JSONUtils.checkJsonValid(""));
=======
        Assertions.assertTrue(JSONUtils.checkJsonValid("3"));
        Assertions.assertFalse(JSONUtils.checkJsonValid(""));
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testFindValue() {
<<<<<<< HEAD
        Assert.assertNull(JSONUtils.findValue(
=======
        Assertions.assertNull(JSONUtils.findValue(
>>>>>>> refs/remotes/origin/3.1.1-release
                new ArrayNode(new JsonNodeFactory(true)), null));
    }

    @Test
    public void testToMap() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");

<<<<<<< HEAD
        Assert.assertTrue(map.equals(JSONUtils.toMap(
                "{\n" + "\"foo\": \"bar\"\n" + "}")));

        Assert.assertFalse(map.equals(JSONUtils.toMap(
                "{\n" + "\"bar\": \"foo\"\n" + "}")));

        Assert.assertNull(JSONUtils.toMap("3"));
        Assert.assertNull(JSONUtils.toMap(null));

        String str = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"}";
        Map<String, String> m = JSONUtils.toMap(str);
        Assert.assertNotNull(m);
=======
        Assertions.assertTrue(map.equals(JSONUtils.toMap(
                "{\n" + "\"foo\": \"bar\"\n" + "}")));

        Assertions.assertFalse(map.equals(JSONUtils.toMap(
                "{\n" + "\"bar\": \"foo\"\n" + "}")));

        Assertions.assertNull(JSONUtils.toMap("3"));
        Assertions.assertNull(JSONUtils.toMap(null));

        String str = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"}";
        Map<String, String> m = JSONUtils.toMap(str);
        Assertions.assertNotNull(m);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testToJsonString() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");

<<<<<<< HEAD
        Assert.assertEquals("{\"foo\":\"bar\"}",
                JSONUtils.toJsonString(map));
        Assert.assertEquals(String.valueOf((Object) null),
                JSONUtils.toJsonString(null));

        Assert.assertEquals("{\"foo\":\"bar\"}",
=======
        Assertions.assertEquals("{\"foo\":\"bar\"}", JSONUtils.toJsonString(map));
        Assertions.assertEquals(String.valueOf((Object) null), JSONUtils.toJsonString(null));

        Assertions.assertEquals("{\"foo\":\"bar\"}",
>>>>>>> refs/remotes/origin/3.1.1-release
                JSONUtils.toJsonString(map, SerializationFeature.WRITE_NULL_MAP_VALUES));
    }

    @Test
    public void parseObject() {
        String str = "{\"color\":\"yellow\",\"type\":\"renault\"}";
        ObjectNode node = JSONUtils.parseObject(str);

<<<<<<< HEAD
        Assert.assertEquals("yellow", node.path("color").asText());

        node.put("price", 100);
        Assert.assertEquals(100, node.path("price").asInt());

        node.put("color", "red");
        Assert.assertEquals("red", node.path("color").asText());
=======
        Assertions.assertEquals("yellow", node.path("color").asText());

        node.put("price", 100);
        Assertions.assertEquals(100, node.path("price").asInt());

        node.put("color", "red");
        Assertions.assertEquals("red", node.path("color").asText());
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void parseArray() {
        String str = "[{\"color\":\"yellow\",\"type\":\"renault\"}]";
        ArrayNode node = JSONUtils.parseArray(str);

<<<<<<< HEAD
        Assert.assertEquals("yellow", node.path(0).path("color").asText());
=======
        Assertions.assertEquals("yellow", node.path(0).path("color").asText());
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void dateToString() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(timeZone);
        JSONUtils.setTimeZone(timeZone);

        String time = "2022-02-22 13:38:24";
        Date date = DateUtils.stringToDate(time);
        String json = JSONUtils.toJsonString(date);
<<<<<<< HEAD
        Assert.assertEquals("\"" + time + "\"", json);

        String errorFormatTime = "Tue Feb 22 03:50:00 UTC 2022";
        Assert.assertNull(DateUtils.stringToDate(errorFormatTime));
=======
        Assertions.assertEquals("\"" + time + "\"", json);

        String errorFormatTime = "Tue Feb 22 03:50:00 UTC 2022";
        Assertions.assertNull(DateUtils.stringToDate(errorFormatTime));
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void stringToDate() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(timeZone);
        JSONUtils.setTimeZone(timeZone);

        String json = "\"2022-02-22 13:38:24\"";
        Date date = JSONUtils.parseObject(json, Date.class);
<<<<<<< HEAD
        Assert.assertEquals(DateUtils.stringToDate("2022-02-22 13:38:24"), date);
=======
        Assertions.assertEquals(DateUtils.stringToDate("2022-02-22 13:38:24"), date);
>>>>>>> refs/remotes/origin/3.1.1-release

    }

}

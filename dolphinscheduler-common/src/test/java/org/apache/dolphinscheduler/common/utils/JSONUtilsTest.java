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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class JSONUtilsTest {


    @Test
    public void createObjectNodeTest() {
        String jsonStr = "{\"a\":\"b\",\"b\":\"d\"}";

        ObjectNode objectNode = JSONUtils.createObjectNode();
        objectNode.put("a", "b");
        objectNode.put("b", "d");
        String s = JSONUtils.toJsonString(objectNode);
        Assert.assertEquals(s, jsonStr);
    }

    @Test
    public void toMap() {

        String jsonStr = "{\"id\":\"1001\",\"name\":\"Jobs\"}";

        Map<String, String> models = JSONUtils.toMap(jsonStr);
        Assert.assertEquals("1001", models.get("id"));
        Assert.assertEquals("Jobs", models.get("name"));

    }


    @Test
    public void string2MapTest() {
        String str = list2String();

        List<LinkedHashMap> maps = JSONUtils.toList(str,
                LinkedHashMap.class);

        Assert.assertEquals(1, maps.size());
        Assert.assertEquals("mysql200", maps.get(0).get("mysql service name"));
        Assert.assertEquals("192.168.xx.xx", maps.get(0).get("mysql address"));
        Assert.assertEquals("3306", maps.get(0).get("port"));
        Assert.assertEquals("80", maps.get(0).get("no index of number"));
        Assert.assertEquals("190", maps.get(0).get("database client connections"));
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
        Assert.assertNull(JSONUtils.parseObject(""));
        Assert.assertNull(JSONUtils.parseObject("foo", String.class));
    }


    @Test
    public void testJsonByteArray() {
        String str = "foo";
        byte[] serializeByte = JSONUtils.toJsonByteArray(str);
        String deserialize = JSONUtils.parseObject(serializeByte, String.class);
        Assert.assertEquals(str, deserialize);
        str = null;
        serializeByte = JSONUtils.toJsonByteArray(str);
        deserialize = JSONUtils.parseObject(serializeByte, String.class);
        Assert.assertNull(deserialize);
    }

    @Test
    public void testToList() {
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("A1B2C3", null));
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("", null));
    }

    @Test
    public void testToMap() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");

        Assert.assertTrue(map.equals(JSONUtils.toMap(
                "{\n" + "\"foo\": \"bar\"\n" + "}")));

        Assert.assertFalse(map.equals(JSONUtils.toMap(
                "{\n" + "\"bar\": \"foo\"\n" + "}")));

        Assert.assertNull(JSONUtils.toMap("3"));
        Assert.assertNull(JSONUtils.toMap(null));

        String str = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"}";
        Map<String, String> m = JSONUtils.toMap(str);
        Assert.assertNotNull(m);
    }

    @Test
    public void testToJsonString() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");

        Assert.assertEquals("{\"foo\":\"bar\"}",
                JSONUtils.toJsonString(map));
        Assert.assertEquals(String.valueOf((Object) null),
                JSONUtils.toJsonString(null));

        Assert.assertEquals("{\"foo\":\"bar\"}",
                JSONUtils.toJsonString(map, SerializationFeature.WRITE_NULL_MAP_VALUES));
    }

    @Test
    public void parseObject() {
        String str = "{\"color\":\"yellow\",\"type\":\"renault\"}";
        ObjectNode node = JSONUtils.parseObject(str);

        Assert.assertEquals("yellow", node.path("color").asText());

        node.put("price", 100);
        Assert.assertEquals(100, node.path("price").asInt());

        node.put("color", "red");
        Assert.assertEquals("red", node.path("color").asText());
    }

    @Test
    public void parseArray() {
        String str = "[{\"color\":\"yellow\",\"type\":\"renault\"}]";
        ArrayNode node = JSONUtils.parseArray(str);

        Assert.assertEquals("yellow", node.path(0).path("color").asText());
    }

    @Test
    public void localDateTimeToString() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        String time = "2022-02-22 13:38:24";
        Date date = DateUtils.stringToDate(time);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        String json = JSONUtils.toJsonString(localDateTime);
        Assert.assertEquals("\"" + time + "\"", json);
    }

    @Test
    public void stringToLocalDateTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        String time = "2022-02-22 13:38:24";
        Date date = DateUtils.stringToDate(time);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        List<LocalDateTime> timeList = JSONUtils.parseObject("[\"2022-02-22 13:38:24\"]", new TypeReference<List<LocalDateTime>>(){});
        Assert.assertNotNull(timeList);
        Assert.assertEquals(1, timeList.size());
        Assert.assertEquals(localDateTime, timeList.get(0));
    }

}

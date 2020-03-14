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

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONUtilsTest {


    @Test
    public void toMap() {

        String jsonStr = "{\"id\":\"1001\",\"name\":\"Jobs\"}";

        Map<String,String> models = JSONUtils.toMap(jsonStr);
        Assert.assertEquals("1001", models.get("id"));
        Assert.assertEquals("Jobs", models.get("name"));

    }

    @Test
    public void convert2Property(){
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str = "{\"direct\":\"IN\",\"prop\":\"ds\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}";
        Property property1 = JSON.parseObject(str, Property.class);
        Direct direct = property1.getDirect();
        Assert.assertEquals(Direct.IN, direct);
    }


    @Test
    public void String2MapTest(){
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

    public String list2String(){

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
        map1.put("mysql service name","mysql200");
        map1.put("mysql address","192.168.xx.xx");
        map1.put("port","3306");
        map1.put("no index of number","80");
        map1.put("database client connections","190");

        List<LinkedHashMap<String, String>> maps = new ArrayList<>();
        maps.add(0,map1);
        String resultJson = JSONUtils.toJson(maps);
        return resultJson;
    }

    @Test
    public void testToJson() {
        Map<String, String> map = new HashMap<>();
        map.put("foo","bar");

        Assert.assertEquals("{\"foo\":\"bar\"}", JSONUtils.toJson(map));
        Assert.assertEquals(
                String.valueOf((Object) null), JSONUtils.toJson(null));
    }

    @Test
    public void testParseObject() {
        Assert.assertEquals("{\"foo\":\"bar\"}", JSONUtils.parseObject(
                "{\n" + "\"foo\": \"bar\",\n" + "}", String.class));

        Assert.assertNull(JSONUtils.parseObject("", null));
        Assert.assertNull(JSONUtils.parseObject("foo", String.class));
    }

    @Test
    public void testToList() {
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("A1B2C3", null));
        Assert.assertEquals(new ArrayList(),
                JSONUtils.toList("", null));
    }

    @Test
    public void testCheckJsonValid() {
        Assert.assertTrue(JSONUtils.checkJsonValid("3"));
        Assert.assertFalse(JSONUtils.checkJsonValid(""));
    }

    @Test
    public void testFindValue() {
        Assert.assertNull(JSONUtils.findValue(
                new ArrayNode(new JsonNodeFactory(true)), null));
    }

    @Test
    public void testToMap() {
        Map<String, String> map = new HashMap<>();
        map.put("foo","bar");

        Assert.assertTrue(map.equals(JSONUtils.toMap(
                "{\n" + "\"foo\": \"bar\",\n" + "}")));

        Assert.assertFalse(map.equals(JSONUtils.toMap(
                "{\n" + "\"bar\": \"foo\",\n" + "}")));

        Assert.assertNull(JSONUtils.toMap("3"));
        Assert.assertNull(JSONUtils.toMap(null));
        Assert.assertNull(JSONUtils.toMap("3", null, null));
        Assert.assertNull(JSONUtils.toMap(null, null, null));
    }

    @Test
    public void testToJsonString() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");

        Assert.assertEquals("{\"foo\":\"bar\"}",
                JSONUtils.toJsonString(map));
        Assert.assertEquals(String.valueOf((Object) null),
                JSONUtils.toJsonString(null));
    }
}

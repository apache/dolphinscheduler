package org.apache.dolphinscheduler.test.utils;

import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;

public class JsonUtil {
    /**
     * jsonToObject
     *
     * @param json
     * @param path       json object access path
     * @param objectType
     * @param <T>
     * @return
     */
    public static <T> T jsonToObject(String json, String path, Class<T> objectType) {
        return JsonPath.from(json).getObject(path, objectType);
    }


    public static <T> List<T> jsonToObjectList(String json, String path, Class<T> objectType) {
        return JsonPath.from(json).getList(path, objectType);
    }


    public static Map<String, Object> jsonToMap(String json, String path) {
        return JsonPath.from(json).getObject(path, new TypeRef<Map<String, Object>>() {});
    }





}

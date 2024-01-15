package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuditLogUtils {

    public static void main(String[] args) {
        User user1 = new User();
        user1.setId(1);
        user1.setUserType(UserType.ADMIN_USER);
        user1.setEmail("ddd");

        User user2 = new User();
        user2.setId(3);
        user2.setUserType(UserType.ADMIN_USER);
        user2.setPhone("1234");
        AuditLogUtils.getDiff(user1, user2);
    }

    public static String getDiff(Object pre, Object now) {
        Map<String, Object> map1 = getAllFieldValues(pre);
        Map<String, Object> map2 = getAllFieldValues(now);

        for (String key1 : map1.keySet()) {
            Object valueOld = map1.get(key1);
            if(map2.containsKey(key1)) {
                Object valueNew = map2.get(key1);
                if (!valueOld.equals(valueNew)) {
                    System.out.println(key1 + " from " + valueOld + " to " + valueNew);
                }
            } else {
                System.out.println(key1 + " value " + valueOld + " deleted");
            }
        }

        for (String key2 : map2.keySet()) {

            if(!map1.containsKey(key2)) {
                Object valueNew = map2.get(key2);
                System.out.println(key2 + " value " + valueNew + " added");
            }
        }
        return "!";
    }

    public static Map<String, Object> getAllFieldValues(Object obj) {
        Class<?> clazz = obj.getClass();

        Map<String, Object> map = new HashMap<>();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    Object value = field.get(obj);

                    if(!Objects.isNull(value)) {
                        map.put(field.getName(), value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 获取父类，直到父类为null
            clazz = clazz.getSuperclass();
        }

        return map;
    }
}
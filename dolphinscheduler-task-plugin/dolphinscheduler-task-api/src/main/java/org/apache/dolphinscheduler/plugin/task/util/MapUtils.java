package org.apache.dolphinscheduler.plugin.task.util;

import java.util.Map;

public class MapUtils {

    public static boolean isEmpty(Map<?, ?> map){
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}

package org.apache.dolphinscheduler.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils {

    public static void combineMap(Map<String, List<String>> originMap, Map<String, List<String>> newMap){
        for (String key : newMap.keySet()){
            List<String> filePaths = originMap.get(key);
            if (filePaths==null){
                filePaths = new ArrayList<>();
                originMap.put(key, filePaths);
            }
            filePaths.addAll(newMap.get(key));
        }
    }
}

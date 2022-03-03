package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ParameterUtils {
    private  static  String NO_VALUE_KEY = "__NO_VALUE_KEY";
    private  static String DOUBLE_HYPHEN  = "--";
    private  static String  HYPHEN = "-";

    public static Map fromArgs(String[] args) throws  IllegalArgumentException {
        Map<String, String> map = new HashMap(args.length / 2);
        int i = 0;
        while (true) {
            while (i < args.length) {
                String key = getKeyFromArgs(args, i);
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("The input  contains an empty argument");
                }

                ++i;
                if (i >= args.length) {
                    map.put(key, NO_VALUE_KEY);
                } else if (NumberUtils.isNumber(args[i])) {
                    map.put(key, args[i]);
                    ++i;
                } else if (!args[i].startsWith(DOUBLE_HYPHEN) && !args[i].startsWith(HYPHEN)) {
                    map.put(key, args[i]);
                    ++i;
                } else {
                    map.put(key, NO_VALUE_KEY);
                }
            }

            return map;
        }
    }

    public static String getKeyFromArgs(String[] args, int index) {
        String key;
        if (args[index].startsWith(DOUBLE_HYPHEN)) {
            key = args[index].substring(2);
        } else {
            if (!args[index].startsWith(HYPHEN)) {
                throw new IllegalArgumentException(String.format("Error parsing arguments '%s' on '%s'. Please prefix keys with -- or -.", Arrays.toString(args), args[index]));
            }

            key = args[index].substring(1);
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException("The input contains an empty argument");
        } else {
            return key;
        }
    }

}

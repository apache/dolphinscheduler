package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> theClass, BaseConnectionParam baseConnectionParam) {
        T result;
        try {
            Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
            if (meth == null) {
                meth = theClass.getDeclaredConstructor(BaseConnectionParam.class);
                meth.setAccessible(true);
                CONSTRUCTOR_CACHE.put(theClass, meth);
            }
            result = meth.newInstance(baseConnectionParam);
        } catch (Exception e) {
            throw new RuntimeException("Datasource plugin constructor nonstandard");
        }
        return result;
    }
}

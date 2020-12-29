package org.apache.dolphinscheduler.microbench.serializer.common;

/**
 * @CalvinKirs
 * @date 2020-12-29 15:18
 */
public interface Serialization {


    <T> byte[] serialize(T obj);


    <T> T deserialize(byte[] bytes, Class<T> clazz);
}

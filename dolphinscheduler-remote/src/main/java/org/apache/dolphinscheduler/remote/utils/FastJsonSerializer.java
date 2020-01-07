package org.apache.dolphinscheduler.remote.utils;

import com.alibaba.fastjson.JSON;

/**
 * @author Tboy
 */
public class FastJsonSerializer {

	public static <T> byte[] serialize(T obj)  {
		String json = JSON.toJSONString(obj);
		return json.getBytes(Constants.UTF8);
	}

	public static <T> String serializeToString(T obj)  {
		return JSON.toJSONString(obj);
	}

	public static <T> T deserialize(byte[] src, Class<T> clazz) {
		return JSON.parseObject(new String(src, Constants.UTF8), clazz);
	}

}

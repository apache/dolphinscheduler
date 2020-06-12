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
package org.apache.dolphinscheduler.remote.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *  json serialize or deserialize
 */
public class JsonSerializer {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

	private JsonSerializer(){

	}
	/**
	 * serialize to byte
	 *
	 * @param obj object
	 * @param <T> object type
	 * @return byte array
	 */
	public static <T> byte[] serialize(T obj)  {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("serializeToString exception!", e);
        }

		return json.getBytes(Constants.UTF8);
	}

	/**
	 *  serialize to string
	 * @param obj object
	 * @param <T> object type
	 * @return string
	 */
	public static <T> String serializeToString(T obj)  {
		String json = "";
		try {
			 json = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			logger.error("serializeToString exception!", e);
		}

		return json;
	}

	/**
	 *  deserialize
	 *
	 * @param src byte array
	 * @param clazz class
	 * @param <T> deserialize type
	 * @return deserialize type
	 */
	public static <T> T deserialize(byte[] src, Class<T> clazz) {

        String json = new String(src, StandardCharsets.UTF_8);
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("deserialize exception!", e);
            return null;
        }

	}

}

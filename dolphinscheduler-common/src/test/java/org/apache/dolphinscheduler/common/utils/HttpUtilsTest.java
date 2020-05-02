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
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient utils test
 */
public class HttpUtilsTest {


	public static final Logger logger = LoggerFactory.getLogger(HttpUtilsTest.class);


	@Test
	public void testGetTest(){
		//success
		String result = HttpUtils.get("https://github.com/manifest.json");
		Assert.assertNotNull(result);
		JSONObject jsonObject = JSON.parseObject(result);
		Assert.assertEquals("GitHub", jsonObject.getString("name"));

		result = HttpUtils.get("https://123.333.111.33/ccc");
		Assert.assertNull(result);
	}
}

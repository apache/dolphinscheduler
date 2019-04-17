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
package cn.escheduler.dao.upgrade.shell;

import cn.escheduler.dao.upgrade.EschedulerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * init escheduler
 *
 */
public class CreateEscheduler {

	private static final Logger logger = LoggerFactory.getLogger(CreateEscheduler.class);

	public static void main(String[] args) {
		Thread.currentThread().setName("manager-CreateEscheduler");
		EschedulerManager eschedulerManager = new EschedulerManager();
		eschedulerManager.initEscheduler();
		logger.info("init escheduler finished");
		try {
			eschedulerManager.upgradeEscheduler();
			logger.info("upgrade escheduler finished");
		} catch (Exception e) {
			logger.error("upgrade escheduler failed",e);
		}

	}
}

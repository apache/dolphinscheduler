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
 * upgrade escheduler database
 */
public class UpgradeEscheduler {
	private static final Logger logger = LoggerFactory.getLogger(UpgradeEscheduler.class);

	public static void main(String[] args) {
		EschedulerManager eschedulerManager = new EschedulerManager();
		try {
			eschedulerManager.upgradeEscheduler();
			logger.info("upgrade escheduler success");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.info("Upgrade escheduler failed");
			throw new RuntimeException(e);
		}

		
	}
	
	
	
}

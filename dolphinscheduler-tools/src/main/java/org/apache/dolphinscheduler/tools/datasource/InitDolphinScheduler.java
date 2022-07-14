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

package org.apache.dolphinscheduler.tools.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class InitDolphinScheduler {
    public static void main(String[] args) {
        SpringApplication.run(InitDolphinScheduler.class, args);
    }

    @Component
    @Profile("init")
    static class InitRunner implements CommandLineRunner {
        private static final Logger logger = LoggerFactory.getLogger(InitRunner.class);

        private final DolphinSchedulerManager dolphinSchedulerManager;

        InitRunner(DolphinSchedulerManager dolphinSchedulerManager) {
            this.dolphinSchedulerManager = dolphinSchedulerManager;
        }

        @Override
        public void run(String... args) {
            dolphinSchedulerManager.initDolphinScheduler();
            logger.info("init DolphinScheduler finished");
        }
    }
}

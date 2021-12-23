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

package org.apache.dolphinscheduler.dao.upgrade.shell;

import org.apache.dolphinscheduler.dao.upgrade.DolphinSchedulerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ComponentScan(value = "org.apache.dolphinscheduler.dao")
@EnableAutoConfiguration(exclude = {QuartzAutoConfiguration.class})
public class CreateDolphinScheduler {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CreateDolphinScheduler.class)
            .profiles("shell-create", "shell-cli")
            .web(WebApplicationType.NONE)
            .run(args);
    }

    @Component
    @Profile("shell-create")
    static class CreateRunner implements CommandLineRunner {
        private static final Logger logger = LoggerFactory.getLogger(CreateRunner.class);

        private final DolphinSchedulerManager dolphinSchedulerManager;

        CreateRunner(DolphinSchedulerManager dolphinSchedulerManager) {
            this.dolphinSchedulerManager = dolphinSchedulerManager;
        }

        @Override
        public void run(String... args) throws Exception {
            if (dolphinSchedulerManager.schemaIsInitialized()) {
                dolphinSchedulerManager.upgradeDolphinScheduler();
                logger.info("upgrade DolphinScheduler finished");
            } else {
                dolphinSchedulerManager.initDolphinScheduler();
                logger.info("init DolphinScheduler finished");
            }
            logger.info("create DolphinScheduler success");
        }
    }
}

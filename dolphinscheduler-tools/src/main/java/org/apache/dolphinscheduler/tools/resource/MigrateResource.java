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

package org.apache.dolphinscheduler.tools.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan("org.apache.dolphinscheduler")
public class MigrateResource {

    public static void main(String[] args) {
        SpringApplication.run(MigrateResource.class, args);
    }

    @Component
    @Profile("resource")
    static class MigrateResourceRunner implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(MigrateResourceRunner.class);

        @Autowired
        private MigrateResourceService migrateResourceService;

        @Override
        public void run(String... args) {
            String targetTenantCode = args[0];
            logger.info("Moving all unmanaged resources to tenant: {}", targetTenantCode);
            migrateResourceService.migrateResourceOnce(targetTenantCode);
        }
    }

}

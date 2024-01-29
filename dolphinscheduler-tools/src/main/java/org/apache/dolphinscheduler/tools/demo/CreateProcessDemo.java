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

package org.apache.dolphinscheduler.tools.demo;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(value = "org.apache.dolphinscheduler", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "org.apache.dolphinscheduler.tools.datasource.*",
        })
})
public class CreateProcessDemo {

    public static void main(String[] args) {
        SpringApplication.run(CreateProcessDemo.class, args);
    }

    @Component
    @Profile("demo")
    @Slf4j
    static class DemoRunner implements CommandLineRunner {

        private final ProcessDefinitionDemo processDefinitionDemo;

        DemoRunner(ProcessDefinitionDemo processDefinitionDemo) {
            this.processDefinitionDemo = processDefinitionDemo;
        }

        @Override
        public void run(String... args) throws Exception {
            processDefinitionDemo.createProcessDefinitionDemo();
            log.info("create process definition demo success");
        }
    }
}

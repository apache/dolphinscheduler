package org.apache.dolphinscheduler.tools.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    static class DemoRunner implements CommandLineRunner {
        private static final Logger logger = LoggerFactory.getLogger(DemoRunner.class);

        private final ProcessDefinitionDemo processDefinitionDemo;

        DemoRunner(ProcessDefinitionDemo processDefinitionDemo) {
            this.processDefinitionDemo = processDefinitionDemo;
        }

        @Override
        public void run(String... args) throws Exception {
            processDefinitionDemo.createProcessDefinitionDemo();
            logger.info("create process definition demo success");
        }
    }
}

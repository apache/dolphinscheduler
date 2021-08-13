package org.apache.dolphinscheduler.graphql;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "org.apache.dolphinscheduler.dao",
        "org.apache.dolphinscheduler.graphql",
        "org.apache.dolphinscheduler.api.service",
        "org.apache.dolphinscheduler.api.utils",
        "org.apache.dolphinscheduler.api.security",
        "org.apache.dolphinscheduler.service"
})
@MapperScan("org.apache.dolphscheduler.*.mapper")
public class DolphinschedulerGraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DolphinschedulerGraphqlApplication.class, args);
    }

}

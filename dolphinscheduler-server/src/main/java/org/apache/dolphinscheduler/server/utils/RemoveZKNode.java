package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.zk.ZookeeperOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("org.apache.dolphinscheduler")
public class RemoveZKNode implements CommandLineRunner {

    private static Integer ARGS_LENGTH = 1;

    private static final Logger logger = LoggerFactory.getLogger(RemoveZKNode.class);


    /**
     * zookeeper operator
     */
    @Autowired
    private ZookeeperOperator zookeeperOperator;

    public static void main(String[] args) {

        new SpringApplicationBuilder(RemoveZKNode.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        if (args.length != ARGS_LENGTH){
            logger.error("Usage: <node>");
            return;
        }

        zookeeperOperator.remove(args[0]);
        zookeeperOperator.close();

    }
}

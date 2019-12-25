package org.apache.dolphinscheduler.server.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 *  monitor server
 */
@ComponentScan("org.apache.dolphinscheduler")
public class MonitorServer implements CommandLineRunner {

    private static Integer ARGS_LENGTH = 4;

    private static final Logger logger = LoggerFactory.getLogger(MonitorServer.class);

    /**
     * monitor
     */
    @Autowired
    private Monitor monitor;

    public static void main(String[] args) throws Exception{

        new SpringApplicationBuilder(MonitorServer.class).web(WebApplicationType.NONE).run(args);

    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length != ARGS_LENGTH){
            logger.error("Usage: <masterPath> <workerPath> <port> <installPath>");
            return;
        }

        String masterPath = args[0];
        String workerPath = args[1];
        Integer port = Integer.parseInt(args[2]);
        String installPath = args[3];
        monitor.monitor(masterPath,workerPath,port,installPath);
    }
}

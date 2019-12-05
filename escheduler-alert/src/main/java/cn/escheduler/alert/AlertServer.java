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
package cn.escheduler.alert;

import cn.escheduler.alert.runner.AlertSender;
import cn.escheduler.alert.utils.Constants;
import cn.escheduler.common.thread.Stopper;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * alert of start
 */
@ComponentScan("cn.escheduler")
public class AlertServer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);
    /**
     * Alert Dao
     */
    private AlertDao alertDao;

    private AlertSender alertSender;

    private static volatile AlertServer instance;

    public AlertServer(AlertDao alertDao) {
        this.alertDao = alertDao;
    }

    public static AlertServer getInstance(AlertDao alertDao){
        if (null == instance) {
            synchronized (AlertServer.class) {
                if(null == instance) {
                    instance = new AlertServer(alertDao);
                }
            }
        }
        return instance;
    }

    public void start(){
        logger.info("Alert Server ready start!");
        while (Stopper.isRunning()){
            try {
                Thread.sleep(Constants.ALERT_SCAN_INTERVEL);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
            List<Alert> alerts = alertDao.listWaitExecutionAlert();
            alertSender = new AlertSender(alerts, alertDao);
            alertSender.run();
        }
    }


    public static void main(String[] args){
        SpringApplication app = new SpringApplication(AlertServer.class);
        app.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        AlertServer alertServer = AlertServer.getInstance(alertDao);
        alertServer.start();
    }
}

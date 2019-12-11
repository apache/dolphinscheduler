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
package org.apache.dolphinscheduler.server.alert;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.zk.AbstractZKClient;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.server.alert.runner.AlertSender;
import org.apache.dolphinscheduler.server.alert.utils.Constants;
import org.apache.dolphinscheduler.server.zk.ZKMasterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * alert of start
 */
@Service
public class AlertServer {

    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);

    /**
     *  alert dao
     */
    @Autowired
    private AlertDao alertDao;

    /**
     *  dolphinscheduler database interface
     */
    @Autowired
    protected ProcessDao processDao;

    /**
     *  zk master client
     */
    private ZKMasterClient zkMasterClient;

    /**
     *  alert thread
     */
    private final Thread alertThread;

    public AlertServer(){
        this.alertThread = new Thread(new AlertTask());
        this.alertThread.setName("alert-thread");
    }

    private class AlertTask implements Runnable{

        @Override
        public void run() {
            while (Stopper.isRunning()){
                InterProcessMutex mutex = null;
                try {
                    zkMasterClient = ZKMasterClient.getZKMasterClient(processDao);
                    mutex = new InterProcessMutex(zkMasterClient.getZkClient(), zkMasterClient.getAlertLockPath());
                    mutex.acquire();

                    List<Alert> alerts = alertDao.listWaitExecutionAlert();
                    AlertSender alertSender = new AlertSender(alerts, alertDao);
                    alertSender.run();
                    Thread.sleep(Constants.ALERT_SCAN_INTERVEL);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                } finally{
                    AbstractZKClient.releaseMutex(mutex);
                }
            }
            logger.info("alert task stop");
        }
    }

    /**
     * start alert server
     */
    public void start(){
        this.alertThread.start();
        logger.info("alert server start ");
    }

    /**
     * close alert server
     */
    public void close(){
        this.alertThread.interrupt();
        logger.info("alert server stopped ");
    }

}

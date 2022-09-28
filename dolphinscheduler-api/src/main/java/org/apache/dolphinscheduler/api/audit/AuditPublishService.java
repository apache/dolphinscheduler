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

package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.api.configuration.AuditConfiguration;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditPublishService {

    private BlockingQueue<AuditMessage> auditMessageQueue = new LinkedBlockingQueue<>();

    @Autowired
    private List<AuditSubscriber> subscribers;

    @Autowired
    private AuditConfiguration auditConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(AuditPublishService.class);

    /**
     * create a daemon thread to process the message queue
     */
    @PostConstruct
    private void init() {
        if (auditConfiguration.getEnabled()) {
            Thread thread = new Thread(this::doPublish);
            thread.setDaemon(true);
            thread.setName("Audit-Log-Consume-Thread");
            thread.start();
        }
    }

    /**
     * publish a new audit message
     *
     * @param message audit message
     */
    public void publish(AuditMessage message) {
        if (auditConfiguration.getEnabled() && !auditMessageQueue.offer(message)) {
            logger.error("Publish audit message failed, message:{}", message);
        }
    }

    /**
     *  subscribers execute the message processor method
     */
    private void doPublish() {
        AuditMessage message = null;
        while (true) {
            try {
                message = auditMessageQueue.take();
                for (AuditSubscriber subscriber : subscribers) {
                    try {
                        subscriber.execute(message);
                    } catch (Exception e) {
                        logger.error("Consume audit message failed, message:{}", message, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("Consume audit message failed, message:{}", message, e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}

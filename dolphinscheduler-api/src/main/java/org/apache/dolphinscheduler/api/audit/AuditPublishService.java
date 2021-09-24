package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.api.configuration.AuditConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
        if (auditConfiguration.isAuditGlobalControlSwitch()) {
            Thread thread = new Thread(() -> doPublish());
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
        if (auditConfiguration.isAuditGlobalControlSwitch()) {
            auditMessageQueue.offer(message);
        }
    }

    /**
     *  subscribers execute the message processor method
     */
    private void doPublish() {
        AuditMessage message;
        while (true) {
            try {
                message = auditMessageQueue.take();
                for (AuditSubscriber subscriber : subscribers) {
                    try {
                        subscriber.execute(message);
                    } catch (Exception e) {
                        logger.error("consume audit message failed {}", message.toString(), e);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("consume audit message failed", e);
            }
        }
    }

}

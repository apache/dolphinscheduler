package org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity;

import java.util.Objects;

/**
 * tenant
 */
public class TenantResponseEntity extends TenantBaseEntity {

    /**
     * id
     */
    private int id;

    /**
     * queue name
     */
    private String queueName;

    /**
     * queue
     */
    private String queue;

    /**
     * create time
     */
    public String createTime;
    /**
     * update time
     */
    public String updateTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TenantResponseEntity tenant = (TenantResponseEntity) o;

        return id == tenant.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


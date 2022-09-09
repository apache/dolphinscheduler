package org.apache.dolphinscheduler.common.thread;

import javax.annotation.Nullable;

public class ThreadNameReplacer implements AutoCloseable {

    private String oldThreadName;
    private @Nullable String newThreadName;

    public ThreadNameReplacer(@Nullable String newThreadName) {
        if (newThreadName == null) {
            return;
        }
        this.newThreadName = newThreadName;
        this.oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(newThreadName);
    }

    @Override
    public void close() {
        if (newThreadName == null) {
            return;
        }
        Thread.currentThread().setName(oldThreadName);
    }
}

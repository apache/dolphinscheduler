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

package org.apache.dolphinscheduler.registry.api.ha;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Serializes role checks and owns the event consumer's startup and shutdown.
 */
@Slf4j
final class RoleChangeEventBusWorker extends BaseDaemonThread implements AutoCloseable {

    private final AbstractHAServer haServer;

    private final RoleChangeEventBus eventBus;

    private final CompletableFuture<Void> initialReconciliation = new CompletableFuture<>();

    private final CompletableFuture<Void> termination = new CompletableFuture<>();

    private volatile boolean closed;

    RoleChangeEventBusWorker(AbstractHAServer haServer, RoleChangeEventBus eventBus) {
        super("HA-RoleChange-Thread");
        this.haServer = haServer;
        this.eventBus = eventBus;
    }

    @Override
    public synchronized void start() {
        if (closed) {
            throw new IllegalStateException("HA event consumer is closed");
        }
        super.start();
        eventBus.publish(RoleChangeEvent.create());
    }

    // Role callbacks also acquire the worker monitor, so wait outside synchronized start().
    void awaitInitialReconciliation() {
        try {
            initialReconciliation.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            close();
            throw new IllegalStateException("Interrupted while starting HA server", ex);
        } catch (ExecutionException ex) {
            close();
            throw new IllegalStateException("Failed to start HA server", ex.getCause());
        }
    }

    boolean isClosed() {
        return closed;
    }

    @Override
    public void run() {
        int failures = 0;
        try {
            while (!closed) {
                eventBus.take();
                if (closed) {
                    break;
                }
                try {
                    boolean shouldRetry = haServer.trySwitchRole();
                    if (shouldRetry && !closed) {
                        eventBus.publish(new RoleChangeEvent(AbstractHAServer.DEFAULT_RETRY_INTERVAL));
                    }
                    failures = 0;
                    initialReconciliation.complete(null);
                } catch (RuntimeException ex) {
                    log.error("Reconcile HA role failed", ex);
                    if (closed) {
                        throw ex;
                    }
                    haServer.changeToStandBy();
                    if (++failures >= AbstractHAServer.DEFAULT_MAX_RETRY_TIMES) {
                        initialReconciliation.completeExceptionally(ex);
                    }
                    if (!closed) {
                        eventBus.publish(new RoleChangeEvent(AbstractHAServer.DEFAULT_RETRY_INTERVAL));
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ex) {
            termination.completeExceptionally(ex);
        } finally {
            try {
                haServer.changeToStandBy();
                termination.complete(null);
            } catch (RuntimeException ex) {
                termination.completeExceptionally(ex);
            } finally {
                initialReconciliation.completeExceptionally(new IllegalStateException("HA server is closed"));
            }
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            closed = true;
            initialReconciliation.completeExceptionally(new IllegalStateException("HA server is closed"));
            if (getState() == State.NEW) {
                termination.complete(null);
            }
        }
        // A role listener can close its own HA server.
        if (Thread.currentThread() == this) {
            return;
        }
        interrupt();
        Uninterruptibles.joinUninterruptibly(this);
        try {
            termination.join();
        } catch (CompletionException ex) {
            throw new IllegalStateException("Failed to stop HA server", ex.getCause());
        }
    }
}

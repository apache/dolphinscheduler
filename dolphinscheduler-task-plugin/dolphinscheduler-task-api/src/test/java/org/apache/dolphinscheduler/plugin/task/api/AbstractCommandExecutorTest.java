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

package org.apache.dolphinscheduler.plugin.task.api;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractCommandExecutorTest {

    @Test
    public void testAwaitCollectedReturnsWhenTheOutputIsCollected() {
        CompletableFuture<Void> collected = new CompletableFuture<>();
        collected.complete(null);

        Assertions.assertTrue(AbstractCommandExecutor.awaitCollected(collected, 1_000L));
    }

    @Test
    public void testAwaitCollectedWithoutOutputCollection() {
        // no process was started, there is nothing to wait for
        Assertions.assertTrue(AbstractCommandExecutor.awaitCollected(null, 1_000L));
    }

    @Test
    public void testAwaitCollectedWhenTheCollectionFailed() {
        // a failed collection has nothing left to wait for, it must not block the caller
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("collect failed"));

        Assertions.assertTrue(AbstractCommandExecutor.awaitCollected(failed, 1_000L));
    }

    @Test
    public void testAwaitCollectedTimesOut() {
        CompletableFuture<Void> pending = new CompletableFuture<>();

        long start = System.currentTimeMillis();
        boolean collected = AbstractCommandExecutor.awaitCollected(pending, 200L);
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertFalse(collected);
        Assertions.assertTrue(elapsed >= 200L, "the wait must last for the whole timeout, elapsed: " + elapsed + "ms");
    }
}

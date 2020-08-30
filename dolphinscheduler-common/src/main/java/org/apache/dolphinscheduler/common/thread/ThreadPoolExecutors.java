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
package org.apache.dolphinscheduler.common.thread;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 * 	thread pool's single instance
 *
 */
public class ThreadPoolExecutors {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutors.class);
    private static Executor executor;
    private static volatile ThreadPoolExecutors threadPoolExecutors;

    private ThreadPoolExecutors(){}


    public static ThreadPoolExecutors getInstance(){
        return getInstance("thread_pool",0);
    }

    public static ThreadPoolExecutors getInstance(String name, int maxThreads){

        if (null == threadPoolExecutors) {

            synchronized (ThreadPoolExecutors.class) {

                if(null == threadPoolExecutors) {
                    threadPoolExecutors = new ThreadPoolExecutors();
                }
                if(null == executor) {
                    executor = new Executor(null == name? "thread_pool" : name, maxThreads == 0? Runtime.getRuntime().availableProcessors() * 3 : maxThreads);
                }
            }
        }

        return threadPoolExecutors;
    }

    /**
     * Executes the given task sometime in the future. The task may execute in a new thread or in an existing pooled thread.
     * If the task cannot be submitted for execution, either because this executor has been shutdown or because its capacity has been reached,
     * the task is handled by the current RejectedExecutionHandler.
     * @param event event
     */
    public void execute(final Runnable event) {
        Executor eventExecutor = getExecutor();
        if (eventExecutor == null) {
            logger.error("Cannot execute [{}}] because the executor is missing.", event);
        } else {
            eventExecutor.execute(event);
        }
    }


    public Future<?> submit(Runnable event) {
        Executor eventExecutor = getExecutor();
        if (eventExecutor == null) {
            logger.error("Cannot submit [{}}] because the executor is missing.", event);
        } else {
            return eventExecutor.submit(event);
        }

        return null;

    }


    public Future<?> submit(Callable<?> task) {
        Executor taskExecutor = getExecutor();
        if (taskExecutor == null) {
            logger.error("Cannot submit [{}] because the executor is missing.", task);
        } else {
            return taskExecutor.submit(task);
        }

        return null;
    }



    public void printStatus() {
        Executor printExecutor = getExecutor();
        printExecutor.getStatus().dumpInfo();
    }


    private Executor getExecutor() {
        return executor;
    }


    public void shutdown() {
        if (executor != null) {
            List<Runnable> wasRunning = executor.threadPoolExecutor
                    .shutdownNow();
            if (!wasRunning.isEmpty()) {
                logger.info("{} had {} on shutdown", executor, wasRunning);
            }
        }
    }


    /**
     * Executor instance.
     */
    private static class Executor {
        /**
         * how long to retain excess threads
         */
        static final long KEEP_ALIVE_TIME_IN_MILLIS = 1000;
        /**
         *  the thread pool executor that services the requests
         */
        final TrackingThreadPoolExecutor threadPoolExecutor;
        /**
         * work queue to use - unbounded queue
         */
        final BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
        private final String name;
        private static final AtomicLong seqids = new AtomicLong(0);
        private final long id;

        protected Executor(String name, int maxThreads) {
            this.id = seqids.incrementAndGet();
            this.name = name;
            //create the thread pool executor
            this.threadPoolExecutor = new TrackingThreadPoolExecutor(
                    maxThreads, maxThreads, KEEP_ALIVE_TIME_IN_MILLIS,
                    TimeUnit.MILLISECONDS, q);
            // name the threads for this threadpool
            ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();
            tfb.setNameFormat(this.name + "-%d");
            this.threadPoolExecutor.setThreadFactory(tfb.build());
        }

        /**
         * Submit the event to the queue for handling.
         *
         * @param event
         */
        void execute(final Runnable event) {
            this.threadPoolExecutor.execute(event);
        }

        Future<?> submit(Runnable event) {
            return this.threadPoolExecutor.submit(event);
        }

        Future<?> submit(Callable<?> event) {
            return  this.threadPoolExecutor.submit(event);
        }


        @Override
        public String toString() {
            return getClass().getSimpleName() + "-" + id + "-" + name;
        }

        public ExecutorStatus getStatus() {
            List<Runnable> queuedEvents = Lists.newArrayList();
            for (Runnable r : q) {
                queuedEvents.add(r);
            }

            List<RunningEventStatus> running = Lists.newArrayList();
            for (Map.Entry<Thread, Runnable> e : threadPoolExecutor
                    .getRunningTasks().entrySet()) {
                Runnable r = e.getValue();
                running.add(new RunningEventStatus(e.getKey(), r));
            }

            return new ExecutorStatus(this, queuedEvents, running);
        }
    }


    /**
     * A subclass of ThreadPoolExecutor that keeps track of the Runnables that
     * are executing at any given point in time.
     */
    static class TrackingThreadPoolExecutor extends ThreadPoolExecutor {
        private ConcurrentMap<Thread, Runnable> running = Maps
                .newConcurrentMap();

        public TrackingThreadPoolExecutor(int corePoolSize,
                                          int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            running.remove(Thread.currentThread());
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            Runnable oldPut = running.put(t, r);
            assert oldPut == null : "inconsistency for thread " + t;
            super.beforeExecute(t, r);
        }

        /**
         * @return a map of the threads currently running tasks inside this
         *         executor. Each key is an active thread, and the value is the
         *         task that is currently running. Note that this is not a
         *         stable snapshot of the map.
         */
        public ConcurrentMap<Thread, Runnable> getRunningTasks() {
            return running;
        }
    }


    /**
     * A snapshot of the status of a particular executor. This includes the
     * contents of the executor's pending queue, as well as the threads and
     * events currently being processed.
     *
     * This is a consistent snapshot that is immutable once constructed.
     */
    public static class ExecutorStatus {
        final Executor executor;
        final List<Runnable> queuedEvents;
        final List<RunningEventStatus> running;

        ExecutorStatus(Executor executor, List<Runnable> queuedEvents,
                       List<RunningEventStatus> running) {
            this.executor = executor;
            this.queuedEvents = queuedEvents;
            this.running = running;
        }

        public void dumpInfo() {

            PrintWriter out = new PrintWriter(System.out);

            out.write("Status for executor: " + executor + "\n");
            out.write("=======================================\n");
            out.write(queuedEvents.size() + " events queued, "
                    + running.size() + " running\n");
            if (!queuedEvents.isEmpty()) {
                out.write("Queued:\n");
                for (Runnable e : queuedEvents) {
                    out.write("  " + e + "\n");
                }
                out.write("\n");
            }
            if (!running.isEmpty()) {
                out.write("Running:\n");
                for (RunningEventStatus stat : running) {
                    out.write("  Running on thread '"
                            + stat.threadInfo.getThreadName() + "': "
                            + stat.event + "\n");
                    out.write(ThreadUtils.formatThreadInfo(
                            stat.threadInfo, "  "));
                    out.write("\n");
                }
            }
            out.flush();
        }
    }


    /**
     * The status of a particular event that is in the middle of being handled
     * by an executor.
     */
    public static class RunningEventStatus {
        final ThreadInfo threadInfo;
        final Runnable event;

        public RunningEventStatus(Thread t, Runnable event) {
            this.threadInfo = ThreadUtils.getThreadInfo(t);
            this.event = event;
        }
    }
}
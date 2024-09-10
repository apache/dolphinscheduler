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

package org.apache.dolphinscheduler.server.master.failover;

import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.MasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.WorkerFailoverEvent;

/**
 * Failover coordinator, responsible for do some failover work when the master or worker server is removed from the cluster.
 * <p> The failover work is aim to make the system continue to work normally after the server is removed.
 */
public interface IFailoverCoordinator {

    /**
     * Global failover, will find out the workflows which should be failover in the global system, then failover them.
     * <p> The global failover is very slow since it will need to scan the whole workflows in the system, it should only be called when the server first startup.
     * And it shouldn't be called in the main thread, since this method might be blocked for a long time.
     */
    void globalMasterFailover(final GlobalMasterFailoverEvent globalMasterFailoverEvent);

    /**
     * Failover master server, will find out the workflows that are running on the failed master server, then failover them.
     * <p> This method is called when a master server is removed from the cluster.
     */
    void failoverMaster(final MasterFailoverEvent masterFailoverEvent);

    /**
     * Failover worker server, will find out the tasks which has been dispatched to the crashed worker and running
     * on the current master, then failover them.
     *
     * <p> This method is called when a worker server is removed from the cluster.
     */
    void failoverWorker(final WorkerFailoverEvent workerFailoverEvent);
}

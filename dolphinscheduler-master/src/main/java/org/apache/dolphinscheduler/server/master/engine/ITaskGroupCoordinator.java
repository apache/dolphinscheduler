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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

/**
 * The TaskGroupCoordinator use to manage the task group slot. The task group slot is used to limit the number of {@link TaskInstance} that can be run at the same time.
 * <p>
 * The {@link TaskGroupQueue} is used to represent the task group slot. When a {@link TaskGroupQueue} which inQueue is YES means the {@link TaskGroupQueue} is using by a {@link TaskInstance}.
 * <p>
 * When the {@link TaskInstance} need to use task group, we should use @{@link ITaskGroupCoordinator#acquireTaskGroupSlot(TaskInstance)} to acquire the task group slot,
 * this method doesn't block should always acquire successfully, and you should directly stop dispatch the task instance.
 * When the task group slot is available, the ITaskGroupCoordinator will wake up the waiting {@link TaskInstance} to dispatch.
 * <pre>
 *     if(needAcquireTaskGroupSlot(taskInstance)) {
 *         taskGroupCoordinator.acquireTaskGroupSlot(taskInstance);
 *         return;
 *     }
 * </pre>
 * <p>
 * When the {@link TaskInstance} is finished, we should use @{@link ITaskGroupCoordinator#releaseTaskGroupSlot(TaskInstance)} to release the task group slot.
 * <pre>
 *     if(needToReleaseTaskGroupSlot(taskInstance)) {
 *         taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);
 *     }
 * </pre>
 */
public interface ITaskGroupCoordinator extends AutoCloseable {

    /**
     * Start the TaskGroupCoordinator, once started, you cannot call this method until you have closed the coordinator.
     */
    void start();

    /**
     * If the {@link TaskInstance#getTaskGroupId()} > 0, and the TaskGroup flag is {@link Flag#YES} then the task instance need to use task group.
     *
     * @param taskInstance task instance
     * @return true if the TaskInstance need to acquireTaskGroupSlot
     */
    boolean needAcquireTaskGroupSlot(final TaskInstance taskInstance);

    /**
     * Acquire the task group slot for the given {@link TaskInstance}.
     * <p>
     * When taskInstance want to acquire a TaskGroup slot, should call this method. If acquire successfully, will create a TaskGroupQueue in db which is in queue and status is {@link TaskGroupQueueStatus#WAIT_QUEUE}.
     * The TaskInstance shouldn't dispatch until there exist available slot, the taskGroupCoordinator notify it.
     *
     * @param taskInstance the task instance which want to acquire task group slot.
     * @throws IllegalArgumentException if the taskInstance is null or the used taskGroup doesn't exist.
     */
    void acquireTaskGroupSlot(TaskInstance taskInstance);

    /**
     * If the TaskInstance is using TaskGroup then it need to release TaskGroupSlot.
     *
     * @param taskInstance taskInstance
     * @return true if the TaskInstance need to release TaskGroupSlot
     */
    boolean needToReleaseTaskGroupSlot(TaskInstance taskInstance);

    /**
     * Release the task group slot for the given {@link TaskInstance}.
     * <p>
     * When taskInstance want to release a TaskGroup slot, should call this method. The release method will delete the taskGroupQueue.
     * This method is idempotent, this means that if the task group slot is already released, this method will do nothing.
     *
     * @param taskInstance the task instance which want to release task group slot.
     * @throws IllegalArgumentException If the taskInstance is null or the task doesn't use task group.
     */
    void releaseTaskGroupSlot(TaskInstance taskInstance);

    /**
     * Close the TaskGroupCoordinator, once closed, the coordinator will not work until you have started the coordinator again.
     */
    @Override
    void close();

}

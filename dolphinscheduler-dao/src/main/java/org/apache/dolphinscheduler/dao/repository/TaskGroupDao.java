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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.TaskGroup;

import java.util.List;

public interface TaskGroupDao extends IDao<TaskGroup> {

    /**
     * Query all TaskGroups
     *
     * @return all TaskGroups
     */
    List<TaskGroup> queryAllTaskGroups();

    /**
     * Query all TaskGroups which useSize > 0
     *
     * @return the TaskGroups which useSize > 0
     */
    List<TaskGroup> queryUsedTaskGroups();

    /**
     * Query all TaskGroups which useSize < groupSize
     *
     * @return the TaskGroups which useSize < groupSize
     */
    List<TaskGroup> queryAvailableTaskGroups();

    /**
     * Acquire a slot for the TaskGroup which useSize should < groupSize, set the useSize = useSize + 1.
     *
     * @param taskGroupId taskGroupId which shouldn't be null
     * @return true if acquire successfully, false otherwise.
     */
    boolean acquireTaskGroupSlot(Integer taskGroupId);

    /**
     * Release a slot for the TaskGroup which useSize should > 0, set the useSize = useSize - 1.
     *
     * @param taskGroupId taskGroupId which shouldn't be null
     * @return true if release successfully, false otherwise.
     */
    boolean releaseTaskGroupSlot(Integer taskGroupId);
}

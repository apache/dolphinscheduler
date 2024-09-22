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

package org.apache.dolphinscheduler.server.master.cluster;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

class WorkerGroupChangeNotifierTest {

    @Test
    void detectWorkerGroupChanges_addedWorkerGroup() {
        WorkerGroupDao workerGroupDao = Mockito.mock(WorkerGroupDao.class);
        WorkerGroupChangeNotifier workerGroupChangeNotifier = new WorkerGroupChangeNotifier(workerGroupDao);

        WorkerGroup workerGroup1 = WorkerGroup.builder()
                .name("workerGroup1")
                .addrList("")
                .build();
        when(workerGroupDao.queryAll()).thenReturn(Lists.newArrayList(workerGroup1));

        AtomicBoolean workerGroupAdded = new AtomicBoolean(false);
        AtomicBoolean workerGroupDeleted = new AtomicBoolean(false);
        AtomicBoolean workerGroupChanged = new AtomicBoolean(false);
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(new WorkerGroupChangeNotifier.WorkerGroupListener() {

            @Override
            public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
                workerGroupDeleted.set(true);
            }

            @Override
            public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
                workerGroupAdded.set(true);
                assertThat(workerGroups).containsExactly(workerGroup1);
            }

            @Override
            public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
                workerGroupChanged.set(true);
            }
        });
        workerGroupChangeNotifier.detectWorkerGroupChanges();
        assertThat(workerGroupAdded.get()).isTrue();
        assertThat(workerGroupChanged.get()).isFalse();
        assertThat(workerGroupDeleted.get()).isFalse();
    }

    @Test
    void detectWorkerGroupChanges_deleteWorkerGroup() {
        WorkerGroupDao workerGroupDao = Mockito.mock(WorkerGroupDao.class);
        WorkerGroupChangeNotifier workerGroupChangeNotifier = new WorkerGroupChangeNotifier(workerGroupDao);

        WorkerGroup workerGroup1 = WorkerGroup.builder()
                .name("workerGroup1")
                .addrList("")
                .build();
        when(workerGroupDao.queryAll()).thenReturn(Lists.newArrayList(workerGroup1));
        workerGroupChangeNotifier.detectWorkerGroupChanges();

        when(workerGroupDao.queryAll()).thenReturn(Lists.newArrayList());
        AtomicBoolean workerGroupAdded = new AtomicBoolean(false);
        AtomicBoolean workerGroupDeleted = new AtomicBoolean(false);
        AtomicBoolean workerGroupChanged = new AtomicBoolean(false);
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(new WorkerGroupChangeNotifier.WorkerGroupListener() {

            @Override
            public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
                workerGroupDeleted.set(true);
                assertThat(workerGroups).containsExactly(workerGroup1);
            }

            @Override
            public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
                workerGroupAdded.set(true);
            }

            @Override
            public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
                workerGroupChanged.set(true);
            }
        });
        workerGroupChangeNotifier.detectWorkerGroupChanges();
        assertThat(workerGroupAdded.get()).isFalse();
        assertThat(workerGroupChanged.get()).isFalse();
        assertThat(workerGroupDeleted.get()).isTrue();
        assertThat(workerGroupChangeNotifier.getWorkerGroupMap()).isEmpty();
    }

    @Test
    void detectWorkerGroupChanges_updateWorkerGroup() {
        WorkerGroupDao workerGroupDao = Mockito.mock(WorkerGroupDao.class);
        WorkerGroupChangeNotifier workerGroupChangeNotifier = new WorkerGroupChangeNotifier(workerGroupDao);

        WorkerGroup workerGroup1 = WorkerGroup.builder()
                .name("workerGroup1")
                .addrList("")
                .build();
        when(workerGroupDao.queryAll()).thenReturn(Lists.newArrayList(workerGroup1));
        workerGroupChangeNotifier.detectWorkerGroupChanges();

        WorkerGroup updatedWorkerGroup1 = WorkerGroup.builder()
                .name("workerGroup1")
                .addrList("127.0.0.1:1235")
                .build();
        when(workerGroupDao.queryAll()).thenReturn(Lists.newArrayList(updatedWorkerGroup1));
        AtomicBoolean workerGroupAdded = new AtomicBoolean(false);
        AtomicBoolean workerGroupDeleted = new AtomicBoolean(false);
        AtomicBoolean workerGroupChanged = new AtomicBoolean(false);
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(new WorkerGroupChangeNotifier.WorkerGroupListener() {

            @Override
            public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
                workerGroupDeleted.set(true);
            }

            @Override
            public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
                workerGroupAdded.set(true);
            }

            @Override
            public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
                workerGroupChanged.set(true);
                assertThat(workerGroups).containsExactly(updatedWorkerGroup1);
            }
        });
        workerGroupChangeNotifier.detectWorkerGroupChanges();
        assertThat(workerGroupAdded.get()).isFalse();
        assertThat(workerGroupChanged.get()).isTrue();
        assertThat(workerGroupDeleted.get()).isFalse();
        assertThat(workerGroupChangeNotifier.getWorkerGroupMap()).containsEntry("workerGroup1", updatedWorkerGroup1);
    }
}

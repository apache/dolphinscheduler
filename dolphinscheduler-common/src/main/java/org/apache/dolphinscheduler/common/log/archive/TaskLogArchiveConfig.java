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

package org.apache.dolphinscheduler.common.log.archive;

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for local task-instance log archiving. Bound from the {@code task-log-archive} prefix in the
 * host server's {@code application.yaml}. The component lives in {@code dolphinscheduler-common} so both the
 * master and the worker (which each hold task logs on their own local disk) share one definition; it is
 * disabled by default so upgraded clusters keep their current behavior until an operator opts in.
 */
@Data
@Component
@ConfigurationProperties(prefix = "task-log-archive")
public class TaskLogArchiveConfig {

    /**
     * Master switch. When false the archiver thread never starts and nothing on disk is touched.
     */
    private boolean enabled = false;

    /**
     * How often the archiver scans the log base. Kept coarse because archiving is a maintenance chore, not a
     * latency-sensitive path.
     */
    private Duration checkInterval = Duration.ofDays(1);

    /**
     * Age threshold for archiving: a day partition is packed into a zip once it is older than this. Measured in
     * whole days from today, so a value of 30 archives partitions dated 31+ days ago.
     */
    private Duration archiveThreshold = Duration.ofDays(30);

    /**
     * Whether archived zips are eventually deleted. This is the only setting that removes log data permanently;
     * off by default so archiving alone (space saving) does not silently drop logs.
     */
    private boolean expireEnabled = false;

    /**
     * Age threshold for deletion: an archive zip is removed once it is older than this. Only consulted when
     * {@link #expireEnabled} is true, and must exceed {@link #archiveThreshold} to be meaningful.
     */
    private Duration expireThreshold = Duration.ofDays(90);
}

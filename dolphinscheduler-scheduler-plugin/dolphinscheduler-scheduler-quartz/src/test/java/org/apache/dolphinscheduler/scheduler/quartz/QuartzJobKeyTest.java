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

package org.apache.dolphinscheduler.scheduler.quartz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;

class QuartzJobKeyTest {

    @Test
    void of() {
        QuartzJobKey quartzJobKey = QuartzJobKey.of(1, 2);
        assertEquals(1, quartzJobKey.getProjectId());
        assertEquals(2, quartzJobKey.getSchedulerId());
    }

    @Test
    void toJobKey() {
        QuartzJobKey quartzJobKey = QuartzJobKey.of(1, 2);
        JobKey jobKey = quartzJobKey.toJobKey();
        assertEquals("job_2", jobKey.getName());
        assertEquals("jobgroup_1", jobKey.getGroup());
    }
}

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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;
import org.apache.dolphinscheduler.api.test.utils.DateUtils;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;

import java.util.Date;

public class ScheduleTimeEntity extends AbstractBaseEntity {
    String complementStartDate;
    String complementEndDate;

    public String getComplementStartDate() {
        return complementStartDate;
    }

    public void setComplementStartDate(Date complementStartDate) {
        this.complementStartDate = DateUtils.dateToString(complementStartDate);
    }

    public String getComplementEndDate() {
        return complementEndDate;
    }

    public void setComplementEndDate(Date complementEndDate) {
        this.complementEndDate = DateUtils.dateToString(complementEndDate);
    }

    public void setComplementStartDate(String complementStartDate) {
        this.complementStartDate = complementStartDate;
    }

    public void setComplementEndDate(String complementEndDate) {
        this.complementEndDate = complementEndDate;
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }
}

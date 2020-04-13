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
package org.apache.dolphinscheduler.api.dto;

import java.util.Date;

import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

/**
 * schedule parameters
 */
@SourceCodeConstraint.RevisedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class ScheduleParam {
  private Date startTime;
  private Date endTime;
  private String crontab;
  private String schedulerCalendar;


  public ScheduleParam() {
  }

  public ScheduleParam(Date startTime, Date endTime, String crontab) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.crontab = crontab;
  }

  public ScheduleParam(Date startTime, Date endTime, String crontab , String schedulerCalendar) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.crontab = crontab;
    this.schedulerCalendar = schedulerCalendar ;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getCrontab() {
    return crontab;
  }

  public void setCrontab(String crontab) {
    this.crontab = crontab;
  }

  public String getSchedulerCalendar() {
    return schedulerCalendar;
  }

  public void setSchedulerCalendar(String schedulerCalendar) {
    this.schedulerCalendar = schedulerCalendar;
  }

  @Override
  public String toString() {
    return "ScheduleParam{" +
            "startTime=" + startTime +
            ", endTime=" + endTime +
            ", crontab='" + crontab + '\'' +
            '}';
  }
}

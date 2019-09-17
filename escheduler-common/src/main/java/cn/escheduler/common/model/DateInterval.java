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
package cn.escheduler.common.model;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * date interval class
 */
public class DateInterval {

    private Instant startInstant;

    private Instant endInstant;

    public DateInterval(Date startDate, Date endDate){
        this.startInstant = startDate.toInstant();
        this.endInstant = endDate.toInstant();
    }

    public DateInterval(Instant startInstant, Instant endTime) {
        this.startInstant = startInstant;
        this.endInstant = endTime;
    }

    public Date getStartTime() {
        return Date.from(startInstant);
    }

    public void setStartTime(Date startTime) {
        this.startInstant = startTime.toInstant();
    }

    public Date getEndTime() {
        return Date.from(endInstant);
    }

    public void setEndTime(Date endTime) {
        this.endInstant = endTime.toInstant();
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    public void setStartInstant(Instant startInstant) {
        this.startInstant = startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    public void setEndInstant(Instant endInstant) {
        this.endInstant = endInstant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startInstant, endInstant);
    }

    @Override
    public String toString() {
        return "DateInterval{" +
                "startInstant=" + startInstant +
                ", endInstant=" + endInstant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateInterval that = (DateInterval) o;
        return startInstant.equals(that.startInstant) &&
                endInstant.equals(that.endInstant);
    }
}

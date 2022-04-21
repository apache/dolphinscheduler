/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.alert.api;

import java.util.Objects;

/**
 * alert data
 */
public class AlertData {
    private int id;
    private String title;
    private String content;
    private String log;
    private int warnType;

    public AlertData(int id, String title, String content, String log, int warnType) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.log = log;
        this.warnType = warnType;
    }

    public AlertData() {
    }

    public static AlertDataBuilder builder() {
        return new AlertDataBuilder();
    }

    public int getId() {
        return this.id;
    }

    public AlertData setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public AlertData setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return this.content;
    }

    public AlertData setContent(String content) {
        this.content = content;
        return this;
    }

    public String getLog() {
        return this.log;
    }

    public AlertData setLog(String log) {
        this.log = log;
        return this;
    }

    public int getWarnType() {
        return warnType;
    }

    public void setWarnType(int warnType) {
        this.warnType = warnType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertData)) {
            return false;
        }
        final AlertData other = (AlertData) o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getId() != other.getId()) {
            return false;
        }
        if (this.getWarnType() != other.getWarnType()) {
            return false;
        }
        final Object thisTitle = this.getTitle();
        final Object otherTitle = other.getTitle();
        if (!Objects.equals(thisTitle, otherTitle)) {
            return false;
        }
        final Object thisContent = this.getContent();
        final Object otherContent = other.getContent();
        if (!Objects.equals(thisContent, otherContent)) {
            return false;
        }
        final Object thisLog = this.getLog();
        final Object otherLog = other.getLog();
        return Objects.equals(thisLog, otherLog);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AlertData;
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        result = result * prime + this.getId();
        result = result * prime + this.getWarnType();
        final Object title = this.getTitle();
        result = result * prime + (title == null ? 43 : title.hashCode());
        final Object content = this.getContent();
        result = result * prime + (content == null ? 43 : content.hashCode());
        final Object log = this.getLog();
        result = result * prime + (log == null ? 43 : log.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AlertData(id=" + this.getId() + ", title=" + this.getTitle() + ", content=" + this.getContent() + ", log=" + this.getLog() + ", warnType=" + this.getWarnType() + ")";
    }

    public static class AlertDataBuilder {
        private int id;
        private String title;
        private String content;
        private String log;
        private int warnType;

        AlertDataBuilder() {
        }

        public AlertDataBuilder id(int id) {
            this.id = id;
            return this;
        }

        public AlertDataBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AlertDataBuilder content(String content) {
            this.content = content;
            return this;
        }

        public AlertDataBuilder log(String log) {
            this.log = log;
            return this;
        }

        public AlertDataBuilder warnType(int warnType) {
            this.warnType = warnType;
            return this;
        }

        public AlertData build() {
            return new AlertData(id, title, content, log, warnType);
        }

        @Override
        public String toString() {
            return "AlertData.AlertDataBuilder(id=" + this.id + ", title=" + this.title + ", content=" + this.content + ", log=" + this.log + ", warnType=" + this.warnType + ")";
        }

    }
}

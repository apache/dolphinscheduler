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

public class AlertData {
    private int id;
    private String title;
    private String content;
    private String log;

    public AlertData(int id, String title, String content, String log) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.log = log;
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

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertData)) {
            return false;
        }
        final AlertData other = (AlertData) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        if (this.getId() != other.getId()) {
            return false;
        }
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) {
            return false;
        }
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) {
            return false;
        }
        final Object this$log = this.getLog();
        final Object other$log = other.getLog();
        if (this$log == null ? other$log != null : !this$log.equals(other$log)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AlertData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $log = this.getLog();
        result = result * PRIME + ($log == null ? 43 : $log.hashCode());
        return result;
    }

    public String toString() {
        return "AlertData(id=" + this.getId() + ", title=" + this.getTitle() + ", content=" + this.getContent() + ", log=" + this.getLog() + ")";
    }

    public static class AlertDataBuilder {
        private int id;
        private String title;
        private String content;
        private String log;

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

        public AlertData build() {
            return new AlertData(id, title, content, log);
        }

        public String toString() {
            return "AlertData.AlertDataBuilder(id=" + this.id + ", title=" + this.title + ", content=" + this.content + ", log=" + this.log + ")";
        }
    }
}

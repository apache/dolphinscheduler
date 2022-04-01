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

package org.apache.dolphinscheduler.plugin.task.zeppelin;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Collections;
import java.util.List;

public class ZeppelinParameters extends AbstractParameters {

    /**
     * job flow define in json format
     * @see <a href="https://docs.aws.amazon.com/emr/latest/APIReference/API_RunJobFlow.html#API_RunJobFlow_Examples">API_RunJobFlow_Examples</a>
     */
    private String noteId;
    private String paragraphId;

    @Override
    public boolean checkParameters() {

        return StringUtils.isNotEmpty(this.noteId) && StringUtils.isNotEmpty(this.paragraphId);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();

    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(String paragraphId) {
        this.paragraphId = paragraphId;
    }

    @Override
    public String toString() {
        return "ZeppelinParameters{" +
                "noteId='" + noteId + '\'' +
                ", paragraphId='" + paragraphId + '\'' +
                '}';
    }
}

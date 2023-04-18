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

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ZeppelinParameters extends AbstractParameters {

    /**
     * parameters for zeppelin client API
     * @see <a href="https://zeppelin.apache.org/docs/0.9.0/usage/zeppelin_sdk/client_api.html">Zeppelin_Client_API_Examples</a>
     */
    private String noteId;
    private String paragraphId;
    private String restEndpoint;
    private String productionNoteDirectory;
    private String parameters;
    private String username;
    private String password;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(this.noteId) && StringUtils.isNotEmpty(this.restEndpoint);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();
    }

}

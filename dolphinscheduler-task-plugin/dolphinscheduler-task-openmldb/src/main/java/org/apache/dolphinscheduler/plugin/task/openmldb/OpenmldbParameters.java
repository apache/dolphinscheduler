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

package org.apache.dolphinscheduler.plugin.task.openmldb;

import lombok.EqualsAndHashCode;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import lombok.Data;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpenmldbParameters extends AbstractParameters {

    private String zk;
    private String zkPath;
    private String executeMode;
    /**
     * origin sql script
     */
    private String sql;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(zk) && StringUtils.isNotEmpty(zkPath) && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return this.resourceList;
    }
}

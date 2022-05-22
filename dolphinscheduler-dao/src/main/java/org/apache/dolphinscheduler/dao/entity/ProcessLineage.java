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

package org.apache.dolphinscheduler.dao.entity;

/**
 * Process lineage
 */
public class ProcessLineage {

    /**
     * project code
     */
    private long projectCode;

    /**
     * post task code
     */
    private long postTaskCode;

    /**
     * post task version
     */
    private int postTaskVersion;

    /**
     * pre task code
     */
    private long preTaskCode;

    /**
     * pre task version
     */
    private int preTaskVersion;

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * process definition version
     */
    private int processDefinitionVersion;

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public long getPostTaskCode() {
        return postTaskCode;
    }

    public void setPostTaskCode(long postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public int getPostTaskVersion() {
        return postTaskVersion;
    }

    public void setPostTaskVersion(int postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    public long getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(long preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public int getPreTaskVersion() {
        return preTaskVersion;
    }

    public void setPreTaskVersion(int preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }
}

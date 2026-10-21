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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseShellInterceptorBuilder<T extends BaseShellInterceptorBuilder<T, Y>, Y extends BaseShellInterceptor>
        implements
            IShellInterceptorBuilder<T, Y> {

    protected String shellDirectory;

    protected String shellName;

    protected String runUser;

    protected Integer cpuQuota;

    protected Integer memoryQuota;

    protected List<String> systemEnvs = new ArrayList<>();

    protected List<String> customEnvScripts = new ArrayList<>();

    protected String k8sConfigYaml;

    protected Map<String, String> propertyMap = new HashMap<>();

    /**
     * The sensitive plaintext values among {@link #propertyMap}. They are only used to
     * mask the final shell script content before it is logged, never used when writing
     * the shell file itself.
     */
    protected Set<String> sensitiveValues = new HashSet<>();

    protected boolean sudoEnable;

    protected List<String> scripts = new ArrayList<>();

    protected BaseShellInterceptorBuilder() {
    }

    @Override
    public T newBuilder(T builder) {
        T newBuilder = newBuilder();
        newBuilder.shellDirectory = builder.shellDirectory;
        newBuilder.shellName = builder.shellName;
        newBuilder.runUser = builder.runUser;
        newBuilder.cpuQuota = builder.cpuQuota;
        newBuilder.memoryQuota = builder.memoryQuota;
        newBuilder.systemEnvs = builder.systemEnvs;
        newBuilder.customEnvScripts = builder.customEnvScripts;
        newBuilder.k8sConfigYaml = builder.k8sConfigYaml;
        newBuilder.propertyMap = builder.propertyMap;
        newBuilder.sensitiveValues = builder.sensitiveValues;
        newBuilder.sudoEnable = builder.sudoEnable;
        newBuilder.scripts = builder.scripts;
        return newBuilder;
    }

    @Override
    public T shellDirectory(String shellDirectory) {
        this.shellDirectory = shellDirectory;
        return (T) this;
    }

    @Override
    public T shellName(String shellFilename) {
        this.shellName = shellFilename;
        return (T) this;
    }

    @Override
    public T runUser(String systemUser) {
        this.runUser = systemUser;
        return (T) this;
    }

    @Override
    public T cpuQuota(Integer cpuQuota) {
        this.cpuQuota = cpuQuota;
        return (T) this;
    }

    @Override
    public T memoryQuota(Integer memoryQuota) {
        this.memoryQuota = memoryQuota;
        return (T) this;
    }

    @Override
    public T appendSystemEnv(String envFiles) {
        systemEnvs.add(envFiles);
        return (T) this;
    }

    @Override
    public T appendCustomEnvScript(String customEnvScript) {
        customEnvScripts.add(customEnvScript);
        return (T) this;
    }

    @Override
    public T k8sConfigYaml(String k8sConfigYaml) {
        this.k8sConfigYaml = k8sConfigYaml;
        return (T) this;
    }

    @Override
    public T properties(Map<String, String> propertyMap) {
        if (MapUtils.isNotEmpty(propertyMap)) {
            this.propertyMap.putAll(propertyMap);
        }
        return (T) this;
    }

    @Override
    public T sensitiveValues(Collection<String> sensitiveValues) {
        if (CollectionUtils.isNotEmpty(sensitiveValues)) {
            this.sensitiveValues.addAll(sensitiveValues);
        }
        return (T) this;
    }

    @Override
    public T sudoMode(boolean sudoEnable) {
        this.sudoEnable = sudoEnable;
        return (T) this;
    }

    @Override
    public T appendScript(String script) {
        scripts.add(script);
        return (T) this;
    }

}

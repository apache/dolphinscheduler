/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.api;

/**
 * Interface for creating {@link ConfigIssue} instances. This interface is available in all Context objects for
 * configurable components.
 *
 * It's currently parametrized to provide backward compatibility for Stage class.
 */
public interface ConfigIssueContext {
    /**
     * Creates a configuration issue for the given ConfigDef.
     *
     * This method can be used only at initialization time.
     *
     * @param configGroup Configuration group of the service configuration, if applicable.
     * @param configName Configuration name of the service configuration, if applicable.
     * @param errorCode <code>ErrorCode</code> for the issue.
     * @param args Arguments for the <code>ErrorCode</code> message.
     * @return Configuration issue to report back.
     */
    public ConfigIssue createConfigIssue(String configGroup, String configName, ErrorCode errorCode, Object... args);
}
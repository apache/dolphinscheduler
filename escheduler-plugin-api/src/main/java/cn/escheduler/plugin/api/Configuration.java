/*
 * Copyright 2018 StreamSets Inc.
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
 * Interface to retrieve typed configuration from configuration files.
 */
public interface Configuration {

    /**
     * Get given conf key as a String.
     *
     * @param name Name of the configuration property
     * @param defaultValue Default value that should be returned in case that the config is missing
     * @return Actual configured value or the default value
     */
    public String get(String name, String defaultValue);

    /**
     * Get given conf key as a long.
     *
     * @param name Name of the configuration property
     * @param defaultValue Default value that should be returned in case that the config is missing
     * @return Actual configured value or the default value
     */
    public long get(String name, long defaultValue);

    /**
     * Get given conf key as a int.
     *
     * @param name Name of the configuration property
     * @param defaultValue Default value that should be returned in case that the config is missing
     * @return Actual configured value or the default value
     */
    public int get(String name, int defaultValue);

    /**
     * Get given conf key as a boolean.
     *
     * @param name Name of the configuration property
     * @param defaultValue Default value that should be returned in case that the config is missing
     * @return Actual configured value or the default value
     */
    public boolean get(String name, boolean defaultValue);

}

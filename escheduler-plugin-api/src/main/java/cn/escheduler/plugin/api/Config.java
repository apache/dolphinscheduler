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

import cn.escheduler.plugin.api.impl.Utils;

/**
 * A <code>Config</code> bean holds a configuration (both name and value) of a stage.
 * <p/>
 * They are use by {@link StageUpgrader} implementations when upgrading the configuration of an older stage version
 * to the current stage version.
 */
public class Config {
    private final String name;
    private final Object value;

    /**
     * Creates a <code>Config</code> bean.
     *
     * @param name the name of the configuration.
     * @param value the value of the configuration.
     */
    public Config(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name of the configuration.
     *
     * @return the name of the configuration.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the configuration.
     *
     * @return the value of the configuration.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the string representation of the configuration.
     *
     * @return the string representation of the configuration.
     */
    @Override
    public String toString() {
        return Utils.format("Config[name='{}' value='{}']", getName(), getValue());
    }

}
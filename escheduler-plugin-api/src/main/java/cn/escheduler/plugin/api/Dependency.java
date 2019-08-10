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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A <i>Dependency</i> specifies a configuration this config depends on. The name of the config and
 * the values that triggers this config can be specified using this.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Dependency {
    /**
     * The name of the configuration this configuration depends on, if any.
     * <p/>
     * If it depends on another configuration, this configuration will be displayed only if the <i>depends on</i>
     * configuration value matches one of the trigger values.
     * <p/>
     * If using {@link ConfigDefBean} configuration variables can be in different classes and at different depths.
     * For these cases, if the <i>depends on</i> configuration variable is not in the same class as the configuration
     * variable referring to it, use <b>^[CONFIGURATION NAME]</b> to specify the full configuration name from the stage.
     * Or use <b>[CONFIGURATION NAME]^...^</b> to specify a relative configuration name, going back from the current
     * configuration.
     *
     * @see #triggeredByValues()
     */
    String configName();

    /**
     * The trigger values of <i>configName</i> that activate this configuration.
     *
     * @see #configName()
     *
     */
    String[] triggeredByValues();

}
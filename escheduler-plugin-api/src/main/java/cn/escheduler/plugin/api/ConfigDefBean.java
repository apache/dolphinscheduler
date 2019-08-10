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
 * Used to define a configuration bean for stages.
 * <p/>
 * Configuration beans can have arbitrary levels of nesting allowing composition and reuse.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigDefBean {

    /**
     * Allows to redefine the groups available to the configurations of the configuration bean.
     * <p/>
     * The redefinition can be done using an explicit subset of the group names of the parent configuration or stage, or
     * by referencing the group ordinals (using <code>#N</code> where <b>N</b> is the ordinal) of the groups of the
     * parent configuration of stage.
     */
    String[] groups() default {};

}

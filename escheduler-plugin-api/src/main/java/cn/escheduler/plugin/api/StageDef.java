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
 * Annotation to declare Data Collector stages classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StageDef {
    /**
     * Indicates the version of the stage.
     * <p/>
     * The version is used to track the configuration of a stage definition and any necessary upgrade via a
     * {@link StageUpgrader}
     */
    int version();

    /**
     * Indicates the UI default label for the stage.
     */
    String label();

    /**
     * Indicates the UI default description for the stage.
     */
    String description() default "";

    /**
     * Indicates the UI icon for the stage.
     */
    String icon() default "";

    /**
     * Indicates the upgrader implementation class to use to upgrade stage configurations for older stage versions.
     */
    Class<? extends StageUpgrader> upgrader() default StageUpgrader.Default.class;

    /**
     * Relative path to online help for this stage.
     */
    String onlineHelpRefUrl();

    /**
     * Indicates if this stage implementation is beta.
     */
    boolean beta() default false;

    /**
     * Tags describing features of stage like object store, messaging queue.
     */
    String[] tags() default {};
}

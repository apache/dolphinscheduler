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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The <code>ConfigDef</code> annotation is used to define stage configuration variables and their visual
 * representation in the UI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigDef {

    /**
     * Enum defining the possible types of the configuration variable.
     */
    public enum Type {
        BOOLEAN(false),
        NUMBER(0),
        STRING(""),
        LIST(Collections.emptyList()),
        MAP(Collections.emptyList()),
        /**
         * If a configuration variable is defined as <code>Model</code>, the variable must also be annotated with a model
         * annotation.
         *
         * @see FieldSelectorModel
         * @see ValueChooserModel
         * @see MultiValueChooserModel
         * @see PredicateModel
         * @see ListBeanModel
         */
        MODEL(""),
        CHARACTER(' '),
        TEXT(""),
        CREDENTIAL(""),
        ;

        private final transient Object defaultValue;

        Type(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the default value for the configuration type to be used when there is no default value defined.
         *
         * @param variableClass the variable class.
         *
         * @return the default value for the configuration type.
         */
        public Object getDefault(Class variableClass) {
            Object value;
            if (variableClass.isEnum()) {
                value = variableClass.getEnumConstants()[0];
            } else if (Map.class.isAssignableFrom(variableClass)) {
                value = Collections.emptyMap();
            } else if (List.class.isAssignableFrom(variableClass)) {
                value = Collections.emptyList();
            } else {
                value = defaultValue;
            }
            return value;
        }
    }

    /**
     * Enum defining the possible UI syntax highlighting for a configuration of type {@link Type#TEXT}.
     *
     * @see #mode()
     */
    public enum Mode {JAVA, JAVASCRIPT, JSON, PLAIN_TEXT, PYTHON, RUBY, SCALA, SQL, GROOVY, SHELL, KOTLIN}

    /**
     * The configuration type for the configuration variable.
     */
    Type type();

    /**
     * The default value for the configuration variable.
     * <p/>
     * If the variable itself has default value assigned, the default value set here is ignored.
     */
    String defaultValue() default "";

    /**
     * Use instead of defaultValue to read a text resource in with UTF-8 as a String and use it as the defaultValue.
     * Path must be relative to the directory within \resources corresponding to the enclosing ConfigDef's class package.
     */
    String defaultValueFromResource() default "";

    /**
     * Indicates if a configuration value is required or not. This is enforced by the validation logic.
     */
    boolean required();

    /**
     * The default label for the UI.
     */
    String label();

    /**
     * The default tooltip description for the UI.
     */
    String description() default "";

    /**
     * The configuration group for the UI.
     * <p/>
     * If the configuration variable is within a configuration bean, instead hardcoding the group name it is possible
     * to reference the group ordinal (using <code>#N</code> where <b>N</b> is the ordinal) of the groups defined in
     * {@link ConfigDefBean} annotation of the configuration bean variable.
     *
     * @see ConfigDefBean
     */
    String group() default "";

    /**
     * The display position in the UI.
     */
    int displayPosition() default 0;

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
     * @see #triggeredByValue()
     */
    String dependsOn() default "";

    /**
     * The trigger values of the <i>depends on</i> configuration that activate this configuration.
     *
     * @see #dependsOn()
     *
     */
    String[] triggeredByValue() default {};

    /**
     * List of {@linkplain Dependency} instances that can specify the parameters and their respective trigger values that
     * this configName depends on. Only if <i>all</i> of these dependencies match at least one of their respective
     * trigger values will enable this config itself.
     *
     * @see Dependency
     */
    Dependency[] dependencies() default {};

    /**
     * Indicates the minimum value for configuration variables of type {@link Type#NUMBER}.
     */
    long min() default Long.MIN_VALUE;

    /**
     * Indicates the maximum value for configuration variables of type {@link Type#NUMBER}.
     */
    long max() default Long.MAX_VALUE;

    /**
     * Indicates the number of lines in the UI for configuration variables of type {@link Type#TEXT}.
     * <p/>
     * <ul>
     *   <li><b>0</b> - displays 1 line, the text box cannot re-size and it accepts only one line of input</li>
     *   <li><b>1</b> - displays 1 line, the text box can be re-size and it accepts multiple lines of</li>
     *   <li><b>n</b> - displays <b>n</b> lines, the text box can be re-size and it accepts only one line of input</li>
     * </ul>
     */
    int lines() default 0;

    /**
     * Indicates the expected syntax for a configuration of type {@link Type#TEXT}. It is used by the UI to provide
     * syntax highlighting.
     */
    Mode mode() default Mode.PLAIN_TEXT;
}
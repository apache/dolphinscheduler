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

package org.apache.dolphinscheduler.plugin.task.java;

import java.io.File;

public class JavaConstants {

    private JavaConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The constants used to get the Java installation directory
     **/
    public static final String JAVA_HOME_VAR = "${JAVA_HOME}";

    /**
     * this constant represents the use of the java command to run a task
     **/
    public static final String RUN_TYPE_JAVA = "JAVA";

    /**
     * this constant represents the use of the java -jar command to run a task
     **/
    public static final String RUN_TYPE_JAR = "JAR";

    /**
     * This constant is the Classpath or module path delimiter for different operating systems
     **/
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * This constant represents the current directory in the Classpath or module path
     **/
    public static final String CLASSPATH_CURRENT_DIR = ".";

    /**
     * This constant is used to construct the pre-pathname of the Java source file
     **/
    public static final String JAVA_SOURCE_CODE_NAME_TEMPLATE = "%s/%s.java";

    /**
     * This constant is the regular expression to get the class name of the source file
     **/
    public static final String PUBLIC_CLASS_NAME_REGEX = "(.*\\s*public\\s+class\\s+)([a-zA-Z_]+[//w_]*)([.\\s\\S]*)";
}

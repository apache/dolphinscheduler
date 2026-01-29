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

public class JavaConstants {

    private JavaConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The constants used to get the Java installation directory
     **/
    public static final String JAVA_HOME_VAR = "${JAVA_HOME}";

    /**
     * This constant represents the use of the java -jar command to run a task
     **/
    public static final String RUN_TYPE_FAT_JAR = "FAT_JAR";

    /**
     * This constant represents the use of the java -cp command to run a task
     **/
    public static final String RUN_TYPE_NORMAL_JAR = "NORMAL_JAR";

    /**
     * This constant is the Classpath or module path delimiter for different operating systems
     **/
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * This constant represents the current directory in the Classpath or module path
     **/
    public static final String CLASSPATH_CURRENT_DIR = ".";

}

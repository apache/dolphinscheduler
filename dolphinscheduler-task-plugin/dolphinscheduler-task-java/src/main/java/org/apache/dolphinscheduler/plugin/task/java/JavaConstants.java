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

    public static final String JAVA_HOME8 = "JAVA_HOME";
    public static final String JAVA_HOME11 = "JAVA_HOME8";
    public static final String JAVA_HOME13 = "JAVA_HOME11";
    public static final String JAVA_HOME15 = "JAVA_HOME13";
    public static final String JAVA_HOME17 = "JAVA_HOME17";


    public static final String RUN_TYPE_JAVA = "JAVA";
    public static final String RUN_TYPE_JAR = "JAR";
    public static final String RUN_TYPE_JSHELL = "JSHELL";


    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String CLASSPATH_CURRENT_DIR = ".";

    public static final String JAVA_SOURCE_CODE_NAME_TEMPLATE = "%s/java_%s.java";



}

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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.shell.bash.BashShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.cmd.CmdShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.sh.ShShellInterceptorBuilder;

public class ShellInterceptorBuilderFactory {

    private final static String INTERCEPTOR_TYPE = PropertyUtils.getString("shell.interceptor.type", "bash");

    @SuppressWarnings("unchecked")
    public static IShellInterceptorBuilder newBuilder() {
        if (INTERCEPTOR_TYPE.equalsIgnoreCase("bash")) {
            return new BashShellInterceptorBuilder();
        }
        if (INTERCEPTOR_TYPE.equalsIgnoreCase("sh")) {
            return new ShShellInterceptorBuilder();
        }
        if (INTERCEPTOR_TYPE.equalsIgnoreCase("cmd")) {
            return new CmdShellInterceptorBuilder();
        }
        throw new IllegalArgumentException("not support shell type: " + INTERCEPTOR_TYPE);
    }

}

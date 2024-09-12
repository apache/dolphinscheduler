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

package org.apache.dolphinscheduler.plugin.task.api.shell.bash;

import org.apache.dolphinscheduler.plugin.task.api.shell.BaseLinuxShellInterceptorBuilder;
import org.apache.dolphinscheduler.spi.exception.FileOperateException;

import java.io.IOException;
import java.util.List;

public class BashShellInterceptorBuilder
        extends
            BaseLinuxShellInterceptorBuilder<BashShellInterceptorBuilder, BashShellInterceptor> {

    @Override
    public BashShellInterceptorBuilder newBuilder() {
        return new BashShellInterceptorBuilder();
    }

    @Override
    public BashShellInterceptor build() throws FileOperateException, IOException {
        generateShellScript();
        List<String> bootstrapCommand = generateBootstrapCommand();
        return new BashShellInterceptor(bootstrapCommand, shellDirectory);
    }

    @Override
    protected String shellInterpreter() {
        return "bash";
    }

    @Override
    protected String shellExtension() {
        return ".sh";
    }

    @Override
    protected String shellHeader() {
        return "#!/bin/bash";
    }

}

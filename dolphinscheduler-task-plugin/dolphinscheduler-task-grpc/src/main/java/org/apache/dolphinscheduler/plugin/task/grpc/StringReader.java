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

package org.apache.dolphinscheduler.plugin.task.grpc;

import javax.inject.Singleton;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.jetbrains.annotations.Nullable;

import io.protostuff.compiler.parser.FileReader;

@Singleton
public class StringReader implements FileReader {

    final String DEFAULT_NAME = "VirtualFile.proto";

    String protoText;

    public StringReader(String proto) {
        this.protoText = proto;
    }

    @Nullable
    @Override
    public CharStream read(String s) {
        return CharStreams.fromString(protoText);
    }

    public String GetDefaultName() {
        return DEFAULT_NAME;
    }
}

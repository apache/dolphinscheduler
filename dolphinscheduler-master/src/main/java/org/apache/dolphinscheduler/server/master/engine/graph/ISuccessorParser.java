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

package org.apache.dolphinscheduler.server.master.engine.graph;

import java.util.Collections;
import java.util.Set;

import lombok.Data;
import lombok.Getter;

public interface ISuccessorParser {

    Successors parseSuccessors(Long taskCode);

    @Getter
    @Data
    class Successors {

        private final Set<Long> successors;

        private final Set<Long> skipped;

        public static Successors empty() {
            return new Successors(Collections.emptySet(), Collections.emptySet());
        }

        public void addSuccessor(Long taskCode) {
            successors.add(taskCode);
        }

        public void addSkipped(Long taskCode) {
            skipped.add(taskCode);
        }
    }

}

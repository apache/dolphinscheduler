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

package org.apache.dolphinscheduler.server.master.config;

import lombok.Data;

import org.springframework.validation.Errors;

@Data
public class CommandFetchStrategy {

    private CommandFetchStrategyType type = CommandFetchStrategyType.ID_SLOT_BASED;

    private CommandFetchConfig config = new IdSlotBasedFetchConfig();

    public void validate(Errors errors) {
        config.validate(errors);
    }

    public enum CommandFetchStrategyType {
        ID_SLOT_BASED,
        ;
    }

    public interface CommandFetchConfig {

        void validate(Errors errors);

    }

    @Data
    public static class IdSlotBasedFetchConfig implements CommandFetchConfig {

        private int idStep = 1;
        private int fetchSize = 10;

        @Override
        public void validate(Errors errors) {
            if (idStep <= 0) {
                errors.rejectValue("step", null, "step must be greater than 0");
            }
            if (fetchSize <= 0) {
                errors.rejectValue("fetchSize", null, "fetchSize must be greater than 0");
            }
        }
    }

}

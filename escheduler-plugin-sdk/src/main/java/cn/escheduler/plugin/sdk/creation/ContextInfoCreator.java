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
package cn.escheduler.plugin.sdk.creation;

import cn.escheduler.plugin.api.Configuration;
import cn.escheduler.plugin.sdk.runner.StageContext;
import cn.escheduler.plugin.api.Stage;

public class ContextInfoCreator {

    private ContextInfoCreator() {}

    public static Stage.Info createInfo(final String name, final int version, final String instanceName) {
        return new Stage.Info() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getVersion() {
                return version;
            }

            @Override
            public String getInstanceName() {
                return instanceName;
            }

            @Override
            public String getLabel() {
                return instanceName;
            }
        };
    }

    public static StageContext createContext(
            String instanceName,
            boolean isPreview
    ) {
        return new StageContext(
                instanceName,
                0,
                isPreview,
                new Configuration() {
                    @Override
                    public String get(String name, String defaultValue) {
                        return null;
                    }

                    @Override
                    public long get(String name, long defaultValue) {
                        return 0;
                    }

                    @Override
                    public int get(String name, int defaultValue) {
                        return 0;
                    }

                    @Override
                    public boolean get(String name, boolean defaultValue) {
                        return false;
                    }
                }
        );
    }
}

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
package cn.escheduler.plugin.sdk.runner;

import cn.escheduler.plugin.api.impl.ErrorMessage;

/**
 * Delegates for error reporting (exceptions that are not connected to a batch).
 */
public interface ReportErrorDelegate {
    /**
     * Report error for given stage.
     *
     * @param stage State that generated the error.
     * @param errorMessage Generated error.
     */
    public void reportError(String stage, ErrorMessage errorMessage);
}

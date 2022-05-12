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

package org.apache.dolphinscheduler.plugin.task.jupyter;

public class JupyterConstants {

    private JupyterConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * conda init
     */
    public static final String CONDA_INIT = "source";

    /**
     * conda activate
     */
    public static final String CONDA_ACTIVATE = "conda activate";

    /**
     * jointer to combine two command
     */
    public static final String JOINTER = "&&";

    /**
     * papermill
     */
    public static final String PAPERMILL = "papermill";

    /**
     * Parameters to pass to the parameters cell.
     */
    public static final String PARAMETERS = "--parameters";

    /**
     * Name of kernel to run.
     */
    public static final String KERNEL = "--kernel";

    /**
     * The execution engine name to use in evaluating the notebook.
     */
    public static final String ENGINE = "--engine";

    /**
     * Time in seconds to wait for each cell before failing execution (default: forever)
     */
    public static final String EXECUTION_TIMEOUT = "--execution-timeout";

    /**
     * Time in seconds to wait for kernel to start.
     */
    public static final String START_TIMEOUT = "--start-timeout";

    /**
     * Insert the paths of input/output notebooks as PAPERMILL_INPUT_PATH/PAPERMILL_OUTPUT_PATH as notebook parameters.
     */
    public static final String INJECT_PATHS = "--inject-paths";

    /**
     * Flag for turning on the progress bar.
     */
    public static final String PROGRESS_BAR = "--progress-bar";
}

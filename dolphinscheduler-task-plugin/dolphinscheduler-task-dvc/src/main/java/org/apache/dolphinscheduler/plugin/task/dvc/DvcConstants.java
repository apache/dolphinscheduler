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

package org.apache.dolphinscheduler.plugin.task.dvc;

public class DvcConstants {

    private DvcConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final class DVC_TASK_TYPE {

        public static final String UPLOAD = "Upload";
        public static final String DOWNLOAD = "Download";
        public static final String INIT = "Init DVC";
    };

    public static final String CHECK_AND_SET_DVC_REPO =
            "which dvc || { echo \"dvc does not exist\"; exit 1; }; DVC_REPO=%s";

    public static final String SET_DATA_PATH = "DVC_DATA_PATH=%s";

    public static final String SET_DATA_LOCATION = "DVC_DATA_LOCATION=%s";

    public static final String SET_VERSION = "DVC_VERSION=%s";

    public static final String SET_MESSAGE = "DVC_MESSAGE=\"%s\"";

    public static final String GIT_CLONE_DVC_REPO = "git clone $DVC_REPO dvc-repository; cd dvc-repository; pwd";

    public static final String DVC_AUTOSTAGE = "dvc config core.autostage true --local || exit 1";

    public static final String DVC_ADD_DATA = "dvc add $DVC_DATA_PATH -v -o $DVC_DATA_LOCATION --to-remote || exit 1";

    public static final String GIT_UPDATE_FOR_UPDATE_DATA = "git commit -am \"$DVC_MESSAGE\"\n" +
            "git tag \"$DVC_VERSION\" -m \"$DVC_MESSAGE\"\n" +
            "git push --all\n" +
            "git push --tags";

    public static final String DVC_DOWNLOAD =
            "dvc get $DVC_REPO $DVC_DATA_LOCATION -o $DVC_DATA_PATH -v --rev $DVC_VERSION";

    public static final String DVC_INIT = "dvc init || exit 1";

    public static final String DVC_ADD_REMOTE = "dvc remote add origin %s -d";

    public static final String GIT_UPDATE_FOR_INIT_DVC = "git commit -am \"init dvc project and add remote\"; git push";

}

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

package org.apache.dolphinscheduler.plugin.task.pytorch;

import static org.apache.dolphinscheduler.common.shell.AbstractShell.ExitCodeException;

import org.apache.dolphinscheduler.common.utils.OSUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class GitProjectManager {

    public static final String GIT_PATH_LOCAL = "GIT_PROJECT";
    private static final Pattern GIT_CHECK_PATTERN = Pattern.compile("^(git@|https?://)(?![&|])[^&|]+$");
    private String path;
    private String baseDir = ".";

    public static boolean isGitPath(String path) {
        return GIT_CHECK_PATTERN.matcher(path).matches();
    }

    public void prepareProject() throws Exception {
        String savePath = Paths.get(baseDir, GIT_PATH_LOCAL).toString();

        log.info("clone project {} to {}", path, savePath);
        String[] command = {"sh", "-c", String.format("git clone %s %s", getGitUrl(), savePath)};
        try {
            OSUtils.exeShell(command);
        } catch (ExitCodeException e) {
            if (!new File(savePath).exists()) {
                throw e;
            }
        }
        log.info("clone project done");
    }

    public String getGitUrl() {
        String gitUrl;
        if (path.contains("#")) {
            gitUrl = path.split("#")[0];
        } else {
            gitUrl = path;
        }
        return gitUrl;

    }

    public String getGitLocalPath() {
        String gitLocalPath;
        if (path.contains("#")) {
            gitLocalPath = Paths.get(GIT_PATH_LOCAL, path.split("#")[1]).toString();
        } else {
            gitLocalPath = GIT_PATH_LOCAL;
        }
        return Paths.get(baseDir, gitLocalPath).toString();

    }
}

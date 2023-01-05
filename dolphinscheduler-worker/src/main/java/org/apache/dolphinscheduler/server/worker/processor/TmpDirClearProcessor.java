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

package org.apache.dolphinscheduler.server.worker.processor;

import static org.apache.dolphinscheduler.common.constants.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TmpDirClearCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import io.netty.channel.Channel;

/**
 * clear tmp dir
 * this used when process is finished
 */
@Component
public class TmpDirClearProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TmpDirClearProcessor.class);

    @Override
    public void process(Channel channel, Command command) {
        TmpDirClearCommand tmpDirClearCommand = JSONUtils.parseObject(command.getBody(), TmpDirClearCommand.class);
        if (tmpDirClearCommand == null) {
            logger.error("Tmp dir clear command is null!");
            return;
        }
        logger.info("Receive clear tmp dir command: {}", tmpDirClearCommand);
        String tmpDir = FileUtils.getTmpDir(
                FileUtils.getTmpBaseDir(),
                tmpDirClearCommand.getTenantCode(),
                tmpDirClearCommand.getProjectCode(),
                tmpDirClearCommand.getProcessDefineCode(),
                tmpDirClearCommand.getProcessDefineVersion(),
                tmpDirClearCommand.getProcessInstanceId());

        // get exec dir
        if (Strings.isNullOrEmpty(tmpDir)) {
            logger.warn("The tmp dir {} is no need to clear.", tmpDir);
            return;
        }

        if (SINGLE_SLASH.equals(tmpDir)) {
            logger.warn("The tmp dir is '/', direct deletion is not allowed.");
            return;
        }

        try {
            File delDir = new File(tmpDir);
            String parentPath = delDir.getParent();
            org.apache.commons.io.FileUtils.deleteDirectory(delDir);
            FileUtils.deleteEmptyParentDir(parentPath);
            logger.info("Success clear the tmp dir: {}.", tmpDir);
        } catch (IOException e) {
            logger.error("Tmp dir clear failed!");
        }

    }

}

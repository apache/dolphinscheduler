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

package org.apache.dolphinscheduler.plugin.task.linkis;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinkisTaskTest {

    @Test
    public void testBuildLinkisExecuteCommand() throws Exception {
        Assertions.assertEquals("sh ./bin/shell-cli -engineType spark-2.4.3",
                testBuildRunCommandLine(testBuildLinkisParameters()));
    }

    private LinkisParameters testBuildLinkisParameters() {
        LinkisParameters linkisParameters = new LinkisParameters();
        List<LinkisParameters.Param> testParamList = new ArrayList<>();
        LinkisParameters.Param testParam = new LinkisParameters.Param();
        testParam.setProps("-engineType");
        testParam.setValue("spark-2.4.3");
        testParamList.add(testParam);
        linkisParameters.setUseCustom(false);
        linkisParameters.setParamScript(testParamList);
        return linkisParameters;
    }

    private static String testBuildRunCommandLine(LinkisParameters linkisParameters) {
        List<String> args = new ArrayList<>();
        String script = "";
        List<LinkisParameters.Param> paramList = linkisParameters.getParamScript();
        for (LinkisParameters.Param param : paramList) {
            script = script.concat(param.getProps())
                    .concat(Constants.SPACE)
                    .concat(param.getValue());
        }
        args.add("sh ./bin/shell-cli");
        args.add(script);
        return String.join(Constants.SPACE, args);
    }
}

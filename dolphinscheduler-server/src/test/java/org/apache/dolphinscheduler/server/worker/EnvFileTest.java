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
package org.apache.dolphinscheduler.server.worker;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class EnvFileTest {

    private static  final Logger logger = LoggerFactory.getLogger(EnvFileTest.class);

    @Test
    public void test() {
        String path = System.getProperty("user.dir")+"/script/env/dolphinscheduler_env.sh";
        String pythonHome = getPythonHome(path);
        logger.info(pythonHome);
    }

    /**
     *  get python home
     * @param path
     * @return
     */
    private static String getPythonHome(String path){
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            while ((line = br.readLine()) != null){
                if (line.contains("PYTHON_HOME")){
                    sb.append(line);
                    break;
                }
            }
            String result = sb.toString();
            if (StringUtils.isEmpty(result)){
                return null;
            }
            String[] arrs = result.split("=");
            if (arrs.length == 2){
                return arrs[1];
            }

        }catch (IOException e){
            logger.error("read file failed",e);
        }finally {
            try {
                if (br != null){
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return null;
    }
}

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

package org.apache.dolphinscheduler.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.ho.yaml.Yaml;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * read yml file
 */
public class YmlReader {
    public static HashMap<String,HashMap<String, String>> map;
    public String getDataYml(String filePath, String key1, String key2) {
        Yaml yaml = new Yaml();
        Resource resource = new DefaultResourceLoader().getResource("classpath:" + filePath + ".yml");
        try {
            InputStream inputStream = resource.getInputStream();
            map = yaml.loadType(inputStream, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = map.get(key1).get(key2);
        return data;
    }
}

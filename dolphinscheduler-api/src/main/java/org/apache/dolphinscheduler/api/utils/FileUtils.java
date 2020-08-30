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
package org.apache.dolphinscheduler.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * file utils
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * copy source file to target file
     *
     * @param file         file
     * @param destFilename destination file name
     */

    public static void copyFile(MultipartFile file, String destFilename) {
        try {

            File destFile = new File(destFilename);
            File destParentDir = new File(destFile.getParent());

            if (!destParentDir.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(destParentDir);
            }

            Files.copy(file.getInputStream(), Paths.get(destFilename));
        } catch (IOException e) {
            logger.error("failed to copy file , {} is empty file", file.getOriginalFilename(), e);
        }
    }

    /**
     * file to resource
     *
     * @param filename file name
     * @return resource
     * @throws MalformedURLException io exceptions
     */
    public static Resource file2Resource(String filename) throws MalformedURLException {
        Path file = Paths.get(filename);

        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            logger.error("file can not read : {}", filename);

        }
        return null;
    }

    /**
     * file convert String
     * @param file MultipartFile file
     * @return file content string
     */
    public static String file2String(MultipartFile file) {
        StringBuilder strBuilder = new StringBuilder();

        try (InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            BufferedReader streamReader = new BufferedReader(inputStreamReader);
            String inputStr;

            while ((inputStr = streamReader.readLine()) != null) {
                strBuilder.append(inputStr);
            }

        } catch (IOException e) {
            logger.error("file convert to string failed: {}", file.getName());
        }

        return strBuilder.toString();
    }
}

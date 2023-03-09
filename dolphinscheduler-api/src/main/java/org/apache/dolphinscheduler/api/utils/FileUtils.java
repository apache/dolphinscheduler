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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * file utils
 */
@Slf4j
public class FileUtils {

    /**
     * copy source InputStream to target file
     * @param file
     * @param destFilename
     */
    public static void copyInputStreamToFile(MultipartFile file, String destFilename) {
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(file.getInputStream(), new File(destFilename));
        } catch (IOException e) {
            log.error("failed to copy file , {} is empty file", file.getOriginalFilename(), e);
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
            log.error("File can not be read, fileName:{}", filename);
        }
        return null;
    }

    /**
     * file convert String
     * @param file MultipartFile file
     * @return file content string
     */
    public static String file2String(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("file convert to string failed: {}", file.getName());
        }

        return "";
    }
}

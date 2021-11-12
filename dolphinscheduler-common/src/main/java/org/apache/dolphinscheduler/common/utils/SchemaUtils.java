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

package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata related common classes
 */
public class SchemaUtils {

    private static final Logger logger = LoggerFactory.getLogger(SchemaUtils.class);
    private static final Pattern p = Pattern.compile("\\s*|\t|\r|\n");

    private SchemaUtils() {
        throw new UnsupportedOperationException("Construct SchemaUtils");
    }

    /**
     * Gets upgradable schemas for all upgrade directories
     *
     * @return all schema list
     */
    public static List<String> getAllSchemaList() {
        List<String> schemaDirList = new ArrayList<>();
        File[] schemaDirArr = FileUtils.getAllDir("sql/upgrade");
        if (schemaDirArr == null || schemaDirArr.length == 0) {
            return null;
        }

        for (File file : schemaDirArr) {
            schemaDirList.add(file.getName());
        }

        schemaDirList.sort((o1, o2) -> {
            try {
                String dir1 = String.valueOf(o1);
                String dir2 = String.valueOf(o2);
                String version1 = dir1.split("_")[0];
                String version2 = dir2.split("_")[0];
                if (version1.equals(version2)) {
                    return 0;
                }

                if (SchemaUtils.isAGreatVersion(version1, version2)) {
                    return 1;
                }

                return -1;

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });

        return schemaDirList;
    }

    /**
     * Determine whether schemaVersion is higher than version
     *
     * @param schemaVersion schema version
     * @param version version
     * @return Determine whether schemaVersion is higher than version
     */
    public static boolean isAGreatVersion(String schemaVersion, String version) {
        if (StringUtils.isEmpty(schemaVersion) || StringUtils.isEmpty(version)) {
            throw new RuntimeException("schemaVersion or version is empty");
        }

        String[] schemaVersionArr = schemaVersion.split("\\.");
        String[] versionArr = version.split("\\.");
        int arrLength = Math.min(schemaVersionArr.length, versionArr.length);
        for (int i = 0; i < arrLength; i++) {
            if (Integer.parseInt(schemaVersionArr[i]) > Integer.parseInt(versionArr[i])) {
                return true;
            } else if (Integer.parseInt(schemaVersionArr[i]) < Integer.parseInt(versionArr[i])) {
                return false;
            }
        }

        // If the version and schema version is the same from 0 up to the arrlength-1 element,whoever has a larger arrLength has a larger version number
        return schemaVersionArr.length > versionArr.length;
    }

    /**
     * Gets the current software version number of the system
     *
     * @return current software version
     */
    public static String getSoftVersion() {
        String softVersion;
        try {
            softVersion = FileUtils.readFile2Str(new FileInputStream("sql/soft_version"));
            softVersion = replaceBlank(softVersion);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Failed to get the product version description file. The file could not be found", e);
        }
        return softVersion;
    }

    /**
     * Strips the string of space carriage returns and tabs
     *
     * @param str string
     * @return string removed blank
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {

            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}

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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Strings;

/**
 * Metadata related common classes
 */
@Slf4j
public class SchemaUtils {

    private SchemaUtils() {
        throw new UnsupportedOperationException("Construct SchemaUtils");
    }

    public static List<String> getAllSchemaList() throws IOException {
        final File[] schemaDirArr = new ClassPathResource("sql/upgrade").getFile().listFiles();

        if (schemaDirArr == null || schemaDirArr.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(schemaDirArr).map(File::getName).sorted((o1, o2) -> {
            try {
                String version1 = o1.split("_")[0];
                String version2 = o2.split("_")[0];

                if (version1.equals(version2)) {
                    return 0;
                }

                if (SchemaUtils.isAGreatVersion(version1, version2)) {
                    return 1;
                }

                return -1;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Determine whether schemaVersion is higher than version
     *
     * @param schemaVersion schema version
     * @param version version
     * @return Determine whether schemaVersion is higher than version
     */
    public static boolean isAGreatVersion(String schemaVersion, String version) {
        if (Strings.isNullOrEmpty(schemaVersion) || Strings.isNullOrEmpty(version)) {
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

        // If the version and schema version is the same from 0 up to the arrlength-1 element,whoever has a larger
        // arrLength has a larger version number
        return schemaVersionArr.length > versionArr.length;
    }

    /**
     * Gets the current software version number of the system
     *
     * @return current software version
     */
    public static String getSoftVersion() throws IOException {
        final ClassPathResource softVersionFile = new ClassPathResource("sql/soft_version");
        String softVersion;
        try (InputStream inputStream = softVersionFile.getInputStream()) {
            softVersion = FileUtils.readFile2Str(inputStream);
            softVersion = Strings.nullToEmpty(softVersion).replaceAll("\\s+|\r|\n", "");
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to get the product version description file. The file could not be found", e);
        }
        return softVersion;
    }

}

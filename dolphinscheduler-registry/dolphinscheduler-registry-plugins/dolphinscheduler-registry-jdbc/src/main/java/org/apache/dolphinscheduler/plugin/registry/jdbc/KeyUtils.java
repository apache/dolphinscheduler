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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.registry.api.RegistryConstants;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KeyUtils {

    /**
     * Whether the path is the parent path of the child
     * <p> Only the parentPath is the parent path of the childPath, return true
     * <p> If the parentPath is equal to the childPath, return false
     */
    public static boolean isParent(final String parentPath, final String childPath) {
        if (StringUtils.isEmpty(parentPath)) {
            throw new IllegalArgumentException("Invalid parent path " + parentPath);
        }
        if (StringUtils.isEmpty(childPath)) {
            throw new IllegalArgumentException("Invalid child path " + childPath);
        }
        final String[] parentSplit = parentPath.split(RegistryConstants.PATH_SEPARATOR);
        final String[] childSplit = childPath.split(RegistryConstants.PATH_SEPARATOR);
        if (parentSplit.length >= childSplit.length) {
            return false;
        }
        for (int i = 0; i < parentSplit.length; i++) {
            if (!parentSplit[i].equals(childSplit[i])) {
                return false;
            }
        }
        return true;

    }

    public static boolean isSamePath(final String path1, final String path2) {
        return removeLastSlash(path1).equals(path2);
    }

    private static String removeLastSlash(final String path) {
        checkNotNull(path, "path is null");
        if (!path.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Invalid path " + path);
        }
        int length = path.length() - 1;
        while (length >= 0 && path.charAt(length) == RegistryConstants.PATH_SEPARATOR_CHAR) {
            length--;
        }
        if (length == -1) {
            return RegistryConstants.PATH_SEPARATOR;
        }
        return path.substring(0, length + 1);
    }

}

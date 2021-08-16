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

package org.apache.dolphinscheduler.spi.resource;

import java.util.List;
import java.util.Map;

public interface ResourceStorage {

    /**
     *  init resource storage
     * @param config config params
     */
    void init(Map<String, String> config);

    /**
     * cat file
     * @param filePath file path
     * @return
     */
    byte[] catFile(String filePath);

    List<String> catFile(String filePath, int skipLineNums, int limit);

    /**
     * delete file
     * @param filePath file path
     * @param recursive recursive
     * @return
     */
    boolean deleteFile(String filePath, Boolean recursive);

    boolean exists(String filePath);

    boolean rename(String oldPath, String newPath);

    boolean uploadLocalFile(String localFileName, String resourceStorageName, boolean overwrite);

    boolean downloadFileToLocal(String resourceFilePath,String localFilePath);

    boolean copyFile(String filePath,String targetFilePath,boolean overwrite,boolean deleteSource);
}

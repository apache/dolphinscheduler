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

package org.apache.dolphinscheduler.plugin.task.flink.factory;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.FileUtils;
import org.apache.flink.util.function.FunctionUtils;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import java.io.File;
import java.io.IOException;

public enum YarnClusterDescriptorFactory implements AbstractClusterDescriptorFactory {

    INSTANCE;

    private static final String XML_FILE_EXTENSION = "xml";

    @Override
    public ClusterDescriptor createClusterDescriptor(
                                                     String hadoopConfDir, Configuration flinkConfig) {
        if (StringUtils.isBlank(hadoopConfDir)) {
            throw new RuntimeException("yarn mode must set param of 'hadoopConfDir'");
        }
        try {
            YarnConfiguration yarnConf = parseYarnConfFromConfDir(hadoopConfDir);
            YarnClient yarnClient = createYarnClientFromYarnConf(yarnConf);

            YarnClusterDescriptor clusterDescriptor =
                    new YarnClusterDescriptor(
                            flinkConfig,
                            yarnConf,
                            yarnClient,
                            YarnClientYarnClusterInformationRetriever.create(yarnClient),
                            false);
            return clusterDescriptor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public YarnClient createYarnClientFromYarnConf(YarnConfiguration yarnConf) {
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(yarnConf);
        yarnClient.start();
        return yarnClient;
    }

    public YarnClient createYarnClientFromHadoopConfDir(String hadoopConf) throws IOException {
        YarnConfiguration yarnConf = parseYarnConfFromConfDir(hadoopConf);
        YarnClient yarnClient = createYarnClientFromYarnConf(yarnConf);
        return yarnClient;
    }

    public YarnConfiguration parseYarnConfFromConfDir(String hadoopConfDir) throws IOException {
        YarnConfiguration yarnConf = new YarnConfiguration();
        FileUtils.listFilesInDirectory(new File(hadoopConfDir).toPath(), this::isXmlFile).stream()
                .map(FunctionUtils.uncheckedFunction(FileUtils::toURL))
                .forEach(yarnConf::addResource);
        return yarnConf;
    }

    private boolean isXmlFile(java.nio.file.Path file) {
        return XML_FILE_EXTENSION.equals(
                org.apache.flink.shaded.guava30.com.google.common.io.Files.getFileExtension(
                        file.toString()));
    }
}

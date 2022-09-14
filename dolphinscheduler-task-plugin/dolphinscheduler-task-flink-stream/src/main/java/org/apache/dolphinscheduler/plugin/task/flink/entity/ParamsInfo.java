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

package org.apache.dolphinscheduler.plugin.task.flink.entity;

import org.apache.dolphinscheduler.plugin.task.flink.enums.FlinkStreamDeployMode;

import java.util.Arrays;
import java.util.Properties;

public class ParamsInfo {

    private String name;
    private String queue;
    private FlinkStreamDeployMode runMode;
    private String runJarPath;
    private String flinkConfDir;
    private String flinkJarPath;
    private String flinkVersion;
    private String hadoopConfDir;
    private String applicationId;
    private String flinkJobId;
    private String entryPointClassName;
    private String[] dependFiles;
    private String[] execArgs;
    private Properties confProperties;

    /** security config */
    private boolean openSecurity;

    private String krb5Path;
    private String principal;
    private String keytabPath;
    private boolean cacheUgi;

    /** checkpoint path in hdfs */
    private String hdfsPath;

    /** finished job print log dir */
    private String finishedJobLogDir;

    private ParamsInfo(
                       String name,
                       String queue,
                       FlinkStreamDeployMode runMode,
                       String runJarPath,
                       String flinkConfDir,
                       String flinkJarPath,
                       String flinkVersion,
                       String hadoopConfDir,
                       String applicationId,
                       String flinkJobId,
                       String entryPointClassName,
                       String[] dependFiles,
                       String[] execArgs,
                       Properties confProperties,
                       boolean openSecurity,
                       boolean cacheUgi,
                       String hdfsPath,
                       String finishedJobLogDir) {
        this.name = name;
        this.runMode = runMode;
        this.queue = queue;
        this.runJarPath = runJarPath;
        this.flinkConfDir = flinkConfDir;
        this.flinkJarPath = flinkJarPath;
        this.flinkVersion = flinkVersion;
        this.hadoopConfDir = hadoopConfDir;
        this.applicationId = applicationId;
        this.flinkJobId = flinkJobId;
        this.entryPointClassName = entryPointClassName;
        this.execArgs = execArgs;
        this.dependFiles = dependFiles;
        this.confProperties = confProperties;
        this.openSecurity = openSecurity;
        this.cacheUgi = cacheUgi;
        this.hdfsPath = hdfsPath;
        this.finishedJobLogDir = finishedJobLogDir;
    }

    public String getName() {
        return name;
    }

    public String getQueue() {
        return queue;
    }

    public FlinkStreamDeployMode getRunMode() {
        return runMode;
    }

    public String getRunJarPath() {
        return runJarPath;
    }

    public String getFlinkConfDir() {
        return flinkConfDir;
    }

    public String getFlinkJarPath() {
        return flinkJarPath;
    }

    public String getFlinkVersion() {
        return flinkVersion;
    }

    public String getHadoopConfDir() {
        return hadoopConfDir;
    }

    public String getEntryPointClassName() {
        return entryPointClassName;
    }

    public String[] getDependFiles() {
        return dependFiles;
    }

    public String[] getExecArgs() {
        return execArgs;
    }

    public Properties getConfProperties() {
        return confProperties;
    }

    public boolean isOpenSecurity() {
        return openSecurity;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getFlinkJobId() {
        return flinkJobId;
    }

    public String getHdfsPath() {
        return hdfsPath;
    }

    public String getFinishedJobLogDir() {
        return finishedJobLogDir;
    }

    public boolean isCacheUgi() {
        return cacheUgi;
    }

    @Override
    public String toString() {
        return "ParamsInfo{"
                + "name='"
                + name
                + '\''
                + ", queue='"
                + queue
                + '\''
                + ", runMode='"
                + runMode
                + '\''
                + ", runJarPath='"
                + runJarPath
                + '\''
                + ", flinkConfDir='"
                + flinkConfDir
                + '\''
                + ", flinkJarPath='"
                + flinkJarPath
                + '\''
                + ", flinkVersion='"
                + flinkVersion
                + '\''
                + ", hadoopConfDir='"
                + hadoopConfDir
                + '\''
                + ", applicationId='"
                + applicationId
                + '\''
                + ", flinkJobId='"
                + flinkJobId
                + '\''
                + ", entryPointClassName='"
                + entryPointClassName
                + '\''
                + ", dependFiles="
                + Arrays.toString(dependFiles)
                + ", execArgs="
                + Arrays.toString(execArgs)
                + ", confProperties="
                + confProperties
                + ", openSecurity="
                + openSecurity
                + ", cacheUgi="
                + cacheUgi
                + ", hdfsPath='"
                + hdfsPath
                + '\''
                + ", finishedJobLogDir='"
                + finishedJobLogDir
                + '\''
                + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private String queue;
        private FlinkStreamDeployMode runMode;
        private String runJarPath;
        private String flinkConfDir;
        private String flinkJarPath;
        private String flinkVersion;
        private String hadoopConfDir;
        private String applicationId;
        private String flinkJobId;
        private String entryPointClassName;
        private String[] dependFiles;
        private String[] execArgs;
        private Properties confProperties;
        private boolean openSecurity;
        private boolean cacheUgi;
        private String hdfsPath;
        private String finishedJobLogDir;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setQueue(String queue) {
            this.queue = queue;
            return this;
        }

        public Builder setFlinkConfDir(String flinkConfDir) {
            this.flinkConfDir = flinkConfDir;
            return this;
        }

        public Builder setExecArgs(String[] execArgs) {
            this.execArgs = execArgs;
            return this;
        }

        public Builder setConfProperties(Properties confProperties) {
            this.confProperties = confProperties;
            return this;
        }

        public Builder setFlinkJarPath(String flinkJarPath) {
            this.flinkJarPath = flinkJarPath;
            return this;
        }

        public Builder setRunJarPath(String runJarPath) {
            this.runJarPath = runJarPath;
            return this;
        }

        public Builder setRunMode(FlinkStreamDeployMode runMode) {
            this.runMode = runMode;
            return this;
        }

        public Builder setEntryPointClassName(String entryPointClassName) {
            this.entryPointClassName = entryPointClassName;
            return this;
        }

        public Builder setFlinkVersion(String flinkVersion) {
            this.flinkVersion = flinkVersion;
            return this;
        }

        public Builder setHadoopConfDir(String hadoopConfDir) {
            this.hadoopConfDir = hadoopConfDir;
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder setDependFiles(String[] dependFiles) {
            this.dependFiles = dependFiles;
            return this;
        }

        public Builder setOpenSecurity(boolean openSecurity) {
            this.openSecurity = openSecurity;
            return this;
        }

        public Builder setCacheUgi(boolean cacheUgi) {
            this.cacheUgi = cacheUgi;
            return this;
        }

        public Builder setHdfsPath(String hdfsPath) {
            this.hdfsPath = hdfsPath;
            return this;
        }

        public Builder setFinishedJobLogDir(String finishedJobLogDir) {
            this.finishedJobLogDir = finishedJobLogDir;
            return this;
        }

        public Builder setFlinkJobId(String flinkJobId) {
            this.flinkJobId = flinkJobId;
            return this;
        }

        public ParamsInfo build() {
            return new ParamsInfo(
                    name,
                    queue,
                    runMode,
                    runJarPath,
                    flinkConfDir,
                    flinkJarPath,
                    flinkVersion,
                    hadoopConfDir,
                    applicationId,
                    flinkJobId,
                    entryPointClassName,
                    dependFiles,
                    execArgs,
                    confProperties,
                    openSecurity,
                    cacheUgi,
                    hdfsPath,
                    finishedJobLogDir);
        }
    }
}

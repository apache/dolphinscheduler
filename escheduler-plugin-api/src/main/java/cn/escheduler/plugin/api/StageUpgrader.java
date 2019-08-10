/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.api;

import java.util.List;

/**
 * The <code>StageUpgrader</code> allows stages to upgrade their configuration from previous versions of the stage.
 * <p/>
 * The upgrader is called only when the version of a stage configuration in a pipeline is older than the version of
 * the stage being used in the pipeline (this typically happens immediately after a Data Collector or stage library
 * upgrade).
 */
public interface StageUpgrader {

    /**
     * Error codes used by the upgrader.
     */
    @GenerateResourceBundle
    public enum Error implements ErrorCode {
        UPGRADER_00("Upgrader not implemented for stage '{}:{}' instance '{}'"),
        UPGRADER_01("Cannot upgrade stage '{}:{}' instance '{}' from version '{}' to version '{}'"),
        ;

        private final String message;

        Error(String message) {
            this.message = message;
        }

        @Override
        public String getCode() {
            return name();
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Default <code>StageUpgrader</code>  implementation. It fails all upgrades.
     */
    public static class Default implements StageUpgrader {

        /**
         * This implementation always throws an exception.
         */
        @Override
        public List<Config> upgrade(List<Config> configs, Context context) throws StageException {
            throw new StageException(
                    Error.UPGRADER_00,
                    context.getLibrary(),
                    context.getStageName(),
                    context.getStageInstance()
            );
        }
    }

    /**
     * Upgrade context with various upgrade metadata.
     */
    public static interface Context {

        /**
         * Stage Library name
         */
        public String getLibrary();

        /**
         * Stage name (identifies this stage uniquely in SDC)
         */
        public String getStageName();

        /**
         * Stage instance name (identifies instance of this stage in particular pipeline).
         */
        public String getStageInstance();

        /**
         * Current version of the stored configuration.
         */
        public int getFromVersion();

        /**
         * Desired target version after upgrade.
         */
        public int getToVersion();

        /**
         * Register given service with provided configuration.
         *
         * @param service Service interface that needs to be registered.
         * @param configs Initial configuration for the service.
         */
        public void registerService(Class service, List<Config> configs);
    }

    /**
     * Upgrades the stage configuration from a previous version to current version.
     *
     * @param configs Current configuration that needs to be upgraded.
     * @param context Upgrade context with various metadata.
     * @return The upgraded configuration.
     * @throws StageException if the configurations could not be upgraded.
     * @return
     */
    default public List<Config> upgrade(List<Config> configs, Context context) throws StageException {
        return upgrade(
                context.getLibrary(),
                context.getStageName(),
                context.getStageInstance(),
                context.getFromVersion(),
                context.getToVersion(),
                configs
        );
    }

    /**
     * Upgrades the stage configuration from a previous version to current version.
     *
     * @param library stage library name.
     * @param stageName stage name.
     * @param stageInstance stage instance name.
     * @param fromVersion version recorded in the stage configuration to upgrade.
     * @param toVersion version of of the stage library, the version to upgrade the configuration to.
     * @param configs The configurations to upgrade.
     * @return The upgraded configuration.
     * @throws StageException if the configurations could not be upgraded.
     */
    @Deprecated
    default public List<Config> upgrade(
            String library,
            String stageName,
            String stageInstance,
            int fromVersion,
            int toVersion,
            List<Config> configs
    ) throws StageException {
        // Default behavior is no-op
        return configs;
    }

}

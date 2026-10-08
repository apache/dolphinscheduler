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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ShellUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkArgsUtils {

    private FlinkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String LOCAL_DEPLOY_MODE = "local";
    private static final String FLINK_VERSION_BEFORE_1_10 = "<1.10";
    private static final String FLINK_VERSION_AFTER_OR_EQUALS_1_12 = ">=1.12";
    private static final String FLINK_VERSION_AFTER_OR_EQUALS_1_13 = ">=1.13";
    /**
     *  default flink deploy mode
     */
    public static final FlinkDeployMode DEFAULT_DEPLOY_MODE = FlinkDeployMode.CLUSTER;

    /**
     * The name suffix of the generated script which executes a flink command, it is suffixed with a
     * unique value because a script can only be created once in the task working directory.
     */
    private static final String FLINK_COMMAND_SHELL_NAME_SUFFIX = "_flink_command_";

    /**
     * The short and the long form of the options which select the cluster a job was submitted to.
     * They are accepted by the cancel / savepoint command as well, so they are repeated there.
     */
    private static final List<String> CLUSTER_CONNECTION_OPTIONS = Arrays.asList(
            "-m", "--jobmanager",
            "-t", "--target");

    /**
     * The configuration keys which identify the cluster a job was submitted to. They may be given as
     * {@code -D<key>=<value>}. The other {@code -D} options configure the submission only and are
     * not repeated on the cancel / savepoint command.
     */
    private static final Set<String> CLUSTER_CONNECTION_CONFIG_KEYS = new HashSet<>(Arrays.asList(
            "rest.address",
            "rest.port",
            "rest.bind-address",
            "rest.bind-port",
            "jobmanager.rpc.address",
            "jobmanager.rpc.port",
            "yarn.application.id",
            "kubernetes.cluster-id",
            "kubernetes.namespace",
            "kubernetes.context",
            "kubernetes.config.file",
            "execution.target"));

    /**
     * The {@code -D} option of the flink CLI, which allows specifying a configuration value.
     */
    private static final String DYNAMIC_PROPERTY_OPTION = "-D";

    /**
     * The tokens of the extra options of the submission. A quoted value is kept as a single token,
     * so that a value which contains whitespace is not split.
     */
    private static final Pattern OPTION_TOKEN_REGEX = Pattern.compile("\"[^\"]*\"|'[^']*'|\\S+");

    private static final long OUTPUT_READER_JOIN_TIMEOUT_SECONDS = 1L;

    /**
     * build flink run command line
     *
     * @param param flink parameters
     * @return argument list
     */
    public static List<String> buildRunCommandLine(TaskExecutionContext taskExecutionContext, FlinkParameters param) {
        switch (param.getProgramType()) {
            case SQL:
                return buildRunCommandLineForSql(taskExecutionContext, param);
            default:
                return buildRunCommandLineForOthers(taskExecutionContext, param);
        }
    }

    /**
     * build flink cancel command line
     *
     * @param jobId the Flink JobID printed by `flink run`, it is not the YARN/K8s application id
     * @param clusterConnectionOptions the cluster connection options of the submission, may be empty
     * @return argument list
     */
    public static List<String> buildCancelCommandLine(String jobId, List<String> clusterConnectionOptions) {
        List<String> args = new ArrayList<>();
        args.add(FlinkConstants.FLINK_COMMAND);
        args.add(FlinkConstants.FLINK_CANCEL);
        args.add(jobId);
        if (CollectionUtils.isNotEmpty(clusterConnectionOptions)) {
            args.addAll(clusterConnectionOptions);
        }
        return args;
    }

    /**
     * Extract the cluster connection options from the extra options of the submission, so that the
     * cancel / savepoint command targets the same cluster as the submission.
     *
     * <p>Only the options which identify the cluster are kept: {@code -m}/{@code --jobmanager},
     * {@code -t}/{@code --target} and the {@code -D} options which configure the cluster address or
     * the execution target. Submission-only arguments, such as the parallelism, the slots, the
     * memory, the application name, the main class or the savepoint to restore from, are not copied.
     * Reusing the task environment does not preserve the command line connection options.
     *
     * @param others the extra options of the submission, may be null
     * @return the cluster connection options, in the order they appear in the submission
     */
    public static List<String> extractClusterConnectionOptions(String others) {
        List<String> options = new ArrayList<>();
        if (StringUtils.isBlank(others)) {
            return options;
        }
        List<String> tokens = tokenizeOptions(others);
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String option = token.contains("=") ? token.substring(0, token.indexOf('=')) : token;
            if (CLUSTER_CONNECTION_OPTIONS.contains(option)) {
                options.add(token);
                // the value may be given as a separate argument, for example "-m <jobmanager>"
                if (option.equals(token) && i + 1 < tokens.size()) {
                    options.add(tokens.get(++i));
                }
            } else if (isClusterConnectionConfig(token)) {
                options.add(token);
            } else if (DYNAMIC_PROPERTY_OPTION.equals(token) && i + 1 < tokens.size()) {
                // the configuration may also be given as a separate argument, "-D <key>=<value>"
                String value = tokens.get(++i);
                if (isClusterConnectionConfig(DYNAMIC_PROPERTY_OPTION + value)) {
                    options.add(token);
                    options.add(value);
                }
            }
        }
        return options;
    }

    private static List<String> tokenizeOptions(String others) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = OPTION_TOKEN_REGEX.matcher(others);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static boolean isClusterConnectionConfig(String token) {
        if (!token.startsWith(DYNAMIC_PROPERTY_OPTION) || token.length() <= DYNAMIC_PROPERTY_OPTION.length()) {
            return false;
        }
        String config = token.substring(DYNAMIC_PROPERTY_OPTION.length());
        int separator = config.indexOf('=');
        String key = separator < 0 ? config : config.substring(0, separator);
        return CLUSTER_CONNECTION_CONFIG_KEYS.contains(key);
    }

    /**
     * build flink savepoint command line, the savepoint folder should be set in flink conf
     *
     * <p>The Flink JobID identifies the job, the YARN application id only identifies the cluster
     * which runs it, so they are passed as different arguments. The YARN application id is passed
     * as the {@code -yid} targeting option, as documented by "Trigger a Savepoint with YARN":
     * {@code flink savepoint :jobId [:targetDirectory] -yid :yarnAppId}. The cluster connection
     * options of the submission are repeated as well, otherwise the savepoint is triggered against
     * the default cluster of the worker instead of the cluster which runs the job.
     *
     * @param jobId the Flink JobID printed by `flink run`, it is not the YARN/K8s application id
     * @param yarnApplicationId the YARN application id used to target the cluster, may be null
     * @param clusterConnectionOptions the cluster connection options of the submission, may be empty
     * @return argument list
     */
    public static List<String> buildSavePointCommandLine(String jobId, String yarnApplicationId,
                                                         List<String> clusterConnectionOptions) {
        List<String> args = new ArrayList<>();
        args.add(FlinkConstants.FLINK_COMMAND);
        args.add(FlinkConstants.FLINK_SAVEPOINT);
        args.add(jobId);
        if (StringUtils.isNotBlank(yarnApplicationId)) {
            args.add(FlinkConstants.FLINK_YARN_APPLICATION_ID);
            args.add(yarnApplicationId);
        }
        if (CollectionUtils.isNotEmpty(clusterConnectionOptions)) {
            args.addAll(clusterConnectionOptions);
        }
        return args;
    }

    /**
     * Execute a flink command in the same environment as the task itself.
     *
     * <p>The task script is executed by a shell interceptor which sources shell.env_source_list and the
     * task's custom environment, resolves the task parameters and runs as the task's tenant. The
     * cancel / savepoint commands must reuse that environment, otherwise placeholders like
     * ${FLINK_HOME} cannot be resolved when they are only defined in the selected task environment.
     *
     * @param taskExecutionContext task execution context
     * @param args the command arguments
     * @return true if the command finished successfully
     */
    public static boolean executeCommand(TaskExecutionContext taskExecutionContext, List<String> args) {
        return executeCommand(taskExecutionContext, args, OSUtils.isSudoEnable(),
                FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS);
    }

    /**
     * Same as {@link #executeCommand(TaskExecutionContext, List)}, with a custom timeout. The
     * savepoint command uses a much longer timeout than the cancel command.
     */
    public static boolean executeCommand(TaskExecutionContext taskExecutionContext, List<String> args,
                                         int timeoutSeconds) {
        return executeCommand(taskExecutionContext, args, OSUtils.isSudoEnable(), timeoutSeconds);
    }

    /**
     * Same as {@link #executeCommand(TaskExecutionContext, List)}, with the sudo mode injected so that
     * the command execution can be verified without requiring sudo permissions.
     */
    static boolean executeCommand(TaskExecutionContext taskExecutionContext, List<String> args, boolean sudoEnable) {
        return executeCommand(taskExecutionContext, args, sudoEnable, FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS);
    }

    /**
     * Same as {@link #executeCommand(TaskExecutionContext, List, int)}, with the sudo mode injected so
     * that the command execution can be verified without requiring sudo permissions.
     */
    static boolean executeCommand(TaskExecutionContext taskExecutionContext, List<String> args, boolean sudoEnable,
                                  int timeoutSeconds) {
        Process process = null;
        try {
            IShellInterceptorBuilder shellInterceptorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .shellDirectory(taskExecutionContext.getExecutePath())
                    .shellName(taskExecutionContext.getTaskInstanceId() + FLINK_COMMAND_SHELL_NAME_SUFFIX
                            + System.nanoTime())
                    .properties(ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()))
                    .appendScript(String.join(" ", args))
                    .sudoMode(sudoEnable)
                    .runUser(taskExecutionContext.getTenantCode());
            if (CollectionUtils.isNotEmpty(ShellUtils.ENV_SOURCE_LIST)) {
                ShellUtils.ENV_SOURCE_LIST.forEach(shellInterceptorBuilder::appendSystemEnv);
            }
            if (StringUtils.isNotBlank(taskExecutionContext.getEnvironmentConfig())) {
                shellInterceptorBuilder.appendCustomEnvScript(taskExecutionContext.getEnvironmentConfig());
            }
            process = shellInterceptorBuilder.build().execute();
            return waitForCommand(process, args, timeoutSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Execute flink command interrupted, args: {}", args, e);
            return false;
        } catch (Exception e) {
            log.error("Execute flink command error, args: {}", args, e);
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Wait for the command to finish, consuming its output so that the command cannot block on a full
     * pipe, and keep the output for diagnostics.
     */
    private static boolean waitForCommand(Process process, List<String> args,
                                          int timeoutSeconds) throws InterruptedException {
        StringBuilder output = new StringBuilder();
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                // the process has exited and its streams are closed
            }
        }, "flink-command-output-reader");
        outputReader.setDaemon(true);
        outputReader.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        // both streams are merged into stdout by the shell interceptor, so joining the reader is enough
        outputReader.join(TimeUnit.SECONDS.toMillis(OUTPUT_READER_JOIN_TIMEOUT_SECONDS));
        if (!finished) {
            process.destroyForcibly();
            log.error("Execute flink command timeout after {}s, args: {}, output: {}", timeoutSeconds, args, output);
            return false;
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Execute flink command failed, exitCode: {}, args: {}, output: {}", exitCode, args, output);
            return false;
        }
        log.info("Execute flink command successfully, args: {}, output: {}", args, output);
        return true;
    }

    /**
     * build flink run command line for SQL
     *
     * @return argument list
     */
    private static List<String> buildRunCommandLineForSql(TaskExecutionContext taskExecutionContext,
                                                          FlinkParameters flinkParameters) {
        List<String> args = new ArrayList<>();

        args.add(FlinkConstants.FLINK_SQL_COMMAND);

        // -i
        String initScriptFilePath = FileUtils.getInitScriptFilePath(taskExecutionContext);
        args.add(FlinkConstants.FLINK_SQL_INIT_FILE);
        args.add(initScriptFilePath);

        // -f
        String scriptFilePath = FileUtils.getScriptFilePath(taskExecutionContext);
        args.add(FlinkConstants.FLINK_SQL_SCRIPT_FILE);
        args.add(scriptFilePath);

        String others = flinkParameters.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }
        return args;
    }

    public static List<String> buildInitOptionsForSql(FlinkParameters flinkParameters) {
        List<String> initOptions = new ArrayList<>();

        FlinkDeployMode deployMode =
                Optional.ofNullable(flinkParameters.getDeployMode()).orElse(FlinkDeployMode.CLUSTER);

        /**
         * Currently flink sql on yarn only supports yarn-per-job mode
         */
        if (FlinkDeployMode.LOCAL == deployMode) {
            // execution.target
            initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.FLINK_LOCAL));
        } else if (FlinkDeployMode.STANDALONE == deployMode) {
            // standalone exec
        } else {
            // execution.target
            initOptions.add(
                    String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.FLINK_YARN_PER_JOB));

            // taskmanager.numberOfTaskSlots
            int slot = flinkParameters.getSlot();
            if (slot > 0) {
                initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_NUMBEROFTASKSLOTS, slot));
            }

            // yarn.application.name
            String appName = flinkParameters.getAppName();
            if (StringUtils.isNotEmpty(appName)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_NAME, ArgsUtils.escape(appName)));
            }

            // jobmanager.memory.process.size
            String jobManagerMemory = flinkParameters.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_JOBMANAGER_MEMORY_PROCESS_SIZE, jobManagerMemory));
            }

            // taskmanager.memory.process.size
            String taskManagerMemory = flinkParameters.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_MEMORY_PROCESS_SIZE, taskManagerMemory));
            }

            // yarn.application.queue
            String yarnQueue = flinkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_QUEUE, yarnQueue));
            }
        }

        // parallelism.default
        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_PARALLELISM_DEFAULT, parallelism));
        }

        return initOptions;
    }

    private static List<String> buildRunCommandLineForOthers(TaskExecutionContext taskExecutionContext,
                                                             FlinkParameters flinkParameters) {
        List<String> args = new ArrayList<>();

        args.add(FlinkConstants.FLINK_COMMAND);
        FlinkDeployMode deployMode = Optional.ofNullable(flinkParameters.getDeployMode()).orElse(DEFAULT_DEPLOY_MODE);
        String flinkVersion = flinkParameters.getFlinkVersion();
        // build run command
        switch (deployMode) {
            case CLUSTER:
                if (FLINK_VERSION_AFTER_OR_EQUALS_1_12.equals(flinkVersion)
                        || FLINK_VERSION_AFTER_OR_EQUALS_1_13.equals(flinkVersion)) {
                    args.add(FlinkConstants.FLINK_RUN); // run
                    args.add(FlinkConstants.FLINK_EXECUTION_TARGET); // -t
                    args.add(FlinkConstants.FLINK_YARN_PER_JOB); // yarn-per-job
                } else {
                    args.add(FlinkConstants.FLINK_RUN); // run
                    args.add(FlinkConstants.FLINK_RUN_MODE); // -m
                    args.add(FlinkConstants.FLINK_YARN_CLUSTER); // yarn-cluster
                }
                break;
            case APPLICATION:
                args.add(FlinkConstants.FLINK_RUN_APPLICATION); // run-application
                args.add(FlinkConstants.FLINK_EXECUTION_TARGET); // -t
                args.add(FlinkConstants.FLINK_YARN_APPLICATION); // yarn-application
                break;
            case LOCAL:
                args.add(FlinkConstants.FLINK_RUN); // run
                break;
            case STANDALONE:
                args.add(FlinkConstants.FLINK_RUN); // run
                break;
        }

        String others = flinkParameters.getOthers();

        // build args
        switch (deployMode) {
            case CLUSTER:
            case APPLICATION:
                int slot = flinkParameters.getSlot();
                if (slot > 0) {
                    args.add(FlinkConstants.FLINK_YARN_SLOT);
                    args.add(String.format("%d", slot)); // -ys
                }

                String appName = flinkParameters.getAppName();
                if (StringUtils.isNotEmpty(appName)) { // -ynm
                    args.add(FlinkConstants.FLINK_APP_NAME);
                    args.add(ArgsUtils.escape(appName));
                }

                // judge flink version, the parameter -yn has removed from flink 1.10
                if (flinkVersion == null || FLINK_VERSION_BEFORE_1_10.equals(flinkVersion)) {
                    int taskManager = flinkParameters.getTaskManager();
                    if (taskManager > 0) { // -yn
                        args.add(FlinkConstants.FLINK_TASK_MANAGE);
                        args.add(String.format("%d", taskManager));
                    }
                }
                String jobManagerMemory = flinkParameters.getJobManagerMemory();
                if (StringUtils.isNotEmpty(jobManagerMemory)) {
                    args.add(FlinkConstants.FLINK_JOB_MANAGE_MEM);
                    args.add(jobManagerMemory); // -yjm
                }

                String taskManagerMemory = flinkParameters.getTaskManagerMemory();
                if (StringUtils.isNotEmpty(taskManagerMemory)) { // -ytm
                    args.add(FlinkConstants.FLINK_TASK_MANAGE_MEM);
                    args.add(taskManagerMemory);
                }

                break;
            case LOCAL:
                break;
            case STANDALONE:
                break;
        }

        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            args.add(FlinkConstants.FLINK_PARALLELISM);
            args.add(String.format("%d", parallelism)); // -p
        }

        // -s -yqu -yat -yD -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        // determine yarn queue
        determinedYarnQueue(args, flinkParameters, deployMode, flinkVersion);
        ProgramType programType = flinkParameters.getProgramType();
        String mainClass = flinkParameters.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(FlinkConstants.FLINK_MAIN_CLASS); // -c
            args.add(flinkParameters.getMainClass()); // main class
        }

        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            // -py
            if (ProgramType.PYTHON == programType) {
                args.add(FlinkConstants.FLINK_PYTHON);
            }
            ResourceContext resourceContext = taskExecutionContext.getResourceContext();
            args.add(resourceContext.getResourceItem(mainJar.getResourceName()).getResourceAbsolutePathInLocal());
        }

        String mainArgs = flinkParameters.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
            args.add(ParameterUtils.convertParameterPlaceholders(mainArgs, ParameterUtils.convert(paramsMap)));
        }

        return args;
    }

    private static void determinedYarnQueue(List<String> args, FlinkParameters flinkParameters,
                                            FlinkDeployMode deployMode, String flinkVersion) {
        switch (deployMode) {
            case CLUSTER:
                if (FLINK_VERSION_AFTER_OR_EQUALS_1_12.equals(flinkVersion)
                        || FLINK_VERSION_AFTER_OR_EQUALS_1_13.equals(flinkVersion)) {
                    doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS);
                } else {
                    doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE);
                }
                break;
            case APPLICATION:
                doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS);
                break;
        }
    }

    private static void doAddQueue(List<String> args, FlinkParameters flinkParameters, String option) {
        String others = flinkParameters.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(option)) {
            String yarnQueue = flinkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                switch (option) {
                    case FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS:
                        args.add(String.format(FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS + "=%s", yarnQueue));
                        break;
                    case FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE:
                        args.add(FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE);
                        args.add(yarnQueue);
                        break;
                }
            }
        }
    }

}

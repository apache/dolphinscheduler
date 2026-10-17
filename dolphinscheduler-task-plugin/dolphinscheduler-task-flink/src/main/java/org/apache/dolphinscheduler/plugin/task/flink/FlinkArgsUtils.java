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
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ShellUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
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
     * The prefixes of the configuration keys which identify the cluster. Each
     * {@code security.ssl.rest.} key is needed as well, otherwise a submission to a REST endpoint
     * over TLS can connect, while the cancel / savepoint command falls back to a plain connection.
     */
    private static final List<String> CLUSTER_CONNECTION_CONFIG_PREFIXES =
            Collections.singletonList("security.ssl.rest.");

    /**
     * The statements of the Flink SQL Client which matter for the cancellation of the submitted jobs,
     * matched at the beginning of a statement:
     *
     * <ul>
     *     <li>a {@code SET} statement, in the quoted form {@code SET 'rest.address' = 'remote-jm'} and
     *         in the unquoted form {@code SET rest.port = 8081}, which selects the connection;</li>
     *     <li>an update statement ({@code INSERT}, {@code UPDATE} or {@code DELETE}), which submits a
     *         job and makes the SQL Client print the JobID of that job;</li>
     *     <li>the boundaries of a statement set, which submits all its update statements as a single
     *         job.</li>
     * </ul>
     *
     * <p>Anchoring the statement avoids matching the text of a string literal, for example
     * {@code SELECT 'SET rest.address=x'}. Only the single quoted form is a SQL Client string, the
     * double quoted form is not part of its syntax.
     *
     * <p>The script is scanned with a pattern, not with a SQL parser, so a string literal which spans
     * several lines and whose line begins with a statement is indistinguishable from a statement.
     */
    private static final Pattern SQL_STATEMENT_REGEX = Pattern.compile(
            "(?im)(?:^|;)\\s*(?:SET\\s+(?:'([^']+)'|([\\w.-]+))\\s*=\\s*(?:'([^']*)'|([^;'\\s]+))"
                    + "|(INSERT|UPDATE|DELETE)\\b"
                    + "|(BEGIN\\s+STATEMENT\\s+SET|EXECUTE\\s+STATEMENT\\s+SET)\\b"
                    + "|(END)\\b)");

    /**
     * The groups of {@link #SQL_STATEMENT_REGEX}: the key and the value of a {@code SET} statement,
     * the keyword of an update statement, and the boundaries of a statement set.
     */
    private static final int SET_QUOTED_KEY_GROUP = 1;
    private static final int SET_KEY_GROUP = 2;
    private static final int SET_QUOTED_VALUE_GROUP = 3;
    private static final int SET_VALUE_GROUP = 4;
    private static final int JOB_STATEMENT_GROUP = 5;
    private static final int STATEMENT_SET_GROUP = 6;
    private static final int STATEMENT_SET_END_GROUP = 7;

    /**
     * The characters which are not interpreted by the shell when a value is given as a single word.
     * A value which contains anything else is quoted, otherwise the shell would split it or expand it
     * before the flink CLI receives it.
     */
    private static final Pattern SHELL_SAFE_VALUE_REGEX = Pattern.compile("[A-Za-z0-9_./:@%+=-]+");

    /**
     * The {@code -D} option of the flink CLI, which allows specifying a configuration value.
     */
    private static final String DYNAMIC_PROPERTY_OPTION = "-D";

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
            String option = unquoteToken(token);
            int separator = option.indexOf('=');
            String optionName = separator < 0 ? option : option.substring(0, separator);
            if (CLUSTER_CONNECTION_OPTIONS.contains(optionName)) {
                options.add(token);
                // the value may be given as a separate argument, for example "-m <jobmanager>"
                if (separator < 0 && i + 1 < tokens.size()) {
                    options.add(tokens.get(++i));
                }
            } else if (isClusterConnectionConfig(option)) {
                options.add(token);
            } else if (DYNAMIC_PROPERTY_OPTION.equals(option) && i + 1 < tokens.size()) {
                // the configuration may also be given as a separate argument, "-D <key>=<value>"
                String value = tokens.get(++i);
                if (isClusterConnectionConfig(DYNAMIC_PROPERTY_OPTION + unquoteToken(value))) {
                    options.add(token);
                    options.add(value);
                }
            }
        }
        return options;
    }

    /**
     * Extract the cluster connection options from the SQL scripts of the submission. A SQL task
     * configures its connection with {@code SET} statements as well, for example
     * {@code SET 'rest.address' = 'remote-jm'}, which the cancel / savepoint command has to repeat.
     *
     * @param scripts the init script and the raw script of the submission, may be null
     * @return the cluster connection options, in the order they appear in the scripts
     */
    public static List<String> extractClusterConnectionOptionsFromSql(String... scripts) {
        List<String> options = new ArrayList<>();
        for (String script : scripts) {
            if (StringUtils.isBlank(script)) {
                continue;
            }
            Matcher matcher = SQL_STATEMENT_REGEX.matcher(script);
            while (matcher.find()) {
                String option = clusterConnectionOptionOfSqlSet(matcher);
                if (option != null) {
                    options.add(option);
                }
            }
        }
        return options;
    }

    /**
     * Extract the cluster connection options which are effective for each job the SQL scripts submit,
     * in the order the jobs are submitted. The scripts are scanned statement by statement and the
     * options are snapshotted when an update statement is found, so a script which changes the
     * connection between two jobs is not collapsed into its last configuration.
     *
     * <p>The Flink SQL Client executes the statements in order and prints the JobID of an update
     * statement when that statement is submitted, so the JobIDs of the task log are in the same order
     * as the snapshots. A job whose statement is not recognized, for example when a script is executed
     * from another file, is not represented and uses the last configuration.
     *
     * @param others the extra options of the submission, may be null
     * @param scripts the init script and the raw script of the submission, may be null
     * @return the options for each submitted job, in the order the jobs are submitted
     */
    public static List<List<String>> extractClusterConnectionOptionsPerJob(String others, String... scripts) {
        List<List<String>> optionsPerJob = new ArrayList<>();
        List<String> current =
                mergeClusterConnectionOptions(extractClusterConnectionOptions(others), Collections.emptyList());
        for (String script : scripts) {
            if (StringUtils.isBlank(script)) {
                continue;
            }
            Matcher matcher = SQL_STATEMENT_REGEX.matcher(script);
            boolean inStatementSet = false;
            boolean statementSetRecorded = false;
            while (matcher.find()) {
                if (matcher.group(JOB_STATEMENT_GROUP) != null) {
                    if (inStatementSet && statementSetRecorded) {
                        // the statements of a statement set are submitted as a single job
                        continue;
                    }
                    optionsPerJob.add(new ArrayList<>(current));
                    statementSetRecorded = inStatementSet;
                    continue;
                }
                if (matcher.group(STATEMENT_SET_GROUP) != null) {
                    inStatementSet = true;
                    statementSetRecorded = false;
                    continue;
                }
                if (matcher.group(STATEMENT_SET_END_GROUP) != null) {
                    inStatementSet = false;
                    statementSetRecorded = false;
                    continue;
                }
                String option = clusterConnectionOptionOfSqlSet(matcher);
                if (option != null) {
                    current = mergeClusterConnectionOptions(current, Collections.singletonList(option));
                }
            }
        }
        return optionsPerJob;
    }

    /**
     * The cluster connection option of a {@code SET} statement of a matched SQL statement, or null when
     * the statement is not a {@code SET} statement or does not configure the connection. The value is
     * quoted for the shell when the generated command needs it.
     */
    private static String clusterConnectionOptionOfSqlSet(Matcher matcher) {
        String key = matcher.group(SET_QUOTED_KEY_GROUP) != null
                ? matcher.group(SET_QUOTED_KEY_GROUP)
                : matcher.group(SET_KEY_GROUP);
        String value = matcher.group(SET_QUOTED_VALUE_GROUP) != null
                ? matcher.group(SET_QUOTED_VALUE_GROUP)
                : matcher.group(SET_VALUE_GROUP);
        if (key == null || value == null || !isClusterConnectionConfig(DYNAMIC_PROPERTY_OPTION + key + "=" + value)) {
            return null;
        }
        return DYNAMIC_PROPERTY_OPTION + key + "=" + quoteForShell(value);
    }

    /**
     * Quote a configuration value for the shell when it contains a character which the shell would
     * interpret, so that the value reaches the flink CLI as a single argument. A plain value, such as
     * a host name or a path, is left as it is.
     */
    private static String quoteForShell(String value) {
        if (SHELL_SAFE_VALUE_REGEX.matcher(value).matches()) {
            return value;
        }
        // a single quote inside the value is closed, escaped and reopened
        return "'" + value.replace("'", "'\\''") + "'";
    }

    /**
     * Merge the cluster connection options, a later option for the same key wins. A connection setting
     * which is given both as a CLI option and as a SQL {@code SET} statement is therefore passed once,
     * with the value which is effective for the submitted job.
     *
     * <p>The options are merged as complete option units: an option which is given as two arguments,
     * for example {@code -D <key>=<value>}, keeps both of them, otherwise merging would leave a value
     * without its {@code -D}.
     */
    public static List<String> mergeClusterConnectionOptions(List<String> first, List<String> second) {
        Map<String, List<String>> optionsByKey = new LinkedHashMap<>();
        collectClusterConnectionOptions(first, optionsByKey);
        collectClusterConnectionOptions(second, optionsByKey);
        List<String> options = new ArrayList<>();
        optionsByKey.values().forEach(options::addAll);
        return options;
    }

    /**
     * Group the tokens into the options they belong to, so that an option which is given as two
     * arguments, for example {@code -m <jobmanager>} or {@code -D <key>=<value>}, is merged as a single
     * unit. The key of a {@code -D} option is the configuration key it sets, the key of the other
     * options is the option itself.
     */
    private static void collectClusterConnectionOptions(List<String> tokens, Map<String, List<String>> optionsByKey) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String option = unquoteToken(token);
            int separator = option.indexOf('=');
            String optionName = separator < 0 ? option : option.substring(0, separator);
            if (CLUSTER_CONNECTION_OPTIONS.contains(optionName)) {
                List<String> unit = new ArrayList<>();
                unit.add(token);
                // the value may be given as a separate argument, for example "-m <jobmanager>"
                if (separator < 0 && i + 1 < tokens.size()) {
                    unit.add(tokens.get(++i));
                }
                optionsByKey.put(optionName, unit);
            } else if (DYNAMIC_PROPERTY_OPTION.equals(option) && i + 1 < tokens.size()) {
                // the configuration may also be given as two arguments, "-D <key>=<value>"
                String value = tokens.get(++i);
                optionsByKey.put(configurationKeyOf(DYNAMIC_PROPERTY_OPTION + unquoteToken(value)),
                        Arrays.asList(token, value));
            } else if (isClusterConnectionConfig(option)) {
                optionsByKey.put(configurationKeyOf(option), Collections.singletonList(token));
            }
        }
    }

    /**
     * The configuration key of a {@code -D<key>=<value>} option, used to merge two options which set
     * the same configuration.
     */
    private static String configurationKeyOf(String option) {
        int separator = option.indexOf('=');
        if (!option.startsWith(DYNAMIC_PROPERTY_OPTION) || separator < 0) {
            return option;
        }
        return option.substring(DYNAMIC_PROPERTY_OPTION.length(), separator);
    }

    /**
     * Remove the quotes which surround a whole token, so that an option or a value which is entirely
     * quoted is still recognized. The original token is kept in the generated command, the shell
     * removes the quotes.
     */
    private static String unquoteToken(String token) {
        if (token.length() > 1 && (token.charAt(0) == '"' || token.charAt(0) == '\'')
                && token.charAt(token.length() - 1) == token.charAt(0)) {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }

    /**
     * Split the extra options of the submission into tokens. A whitespace inside a quoted segment
     * does not separate two tokens, and the quotes are kept so that the generated command stays
     * quoted for the shell. A quote may appear inside a token, for example
     * {@code -Dkubernetes.config.file="/opt/flink configs/kube.yaml"}.
     */
    private static List<String> tokenizeOptions(String others) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        char quote = 0;
        boolean escaped = false;
        for (int i = 0; i < others.length(); i++) {
            char current = others.charAt(i);
            if (escaped) {
                token.append(current);
                escaped = false;
            } else if (current == '\\') {
                token.append(current);
                escaped = true;
            } else if (quote != 0) {
                token.append(current);
                if (current == quote) {
                    quote = 0;
                }
            } else if (current == '"' || current == '\'') {
                token.append(current);
                quote = current;
            } else if (Character.isWhitespace(current)) {
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(current);
            }
        }
        if (token.length() > 0) {
            tokens.add(token.toString());
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
        if (CLUSTER_CONNECTION_CONFIG_KEYS.contains(key)) {
            return true;
        }
        return CLUSTER_CONNECTION_CONFIG_PREFIXES.stream().anyMatch(key::startsWith);
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
        return executeCommandWithOutput(taskExecutionContext, args, OSUtils.isSudoEnable(),
                FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS).isSuccess();
    }

    /**
     * Same as {@link #executeCommand(TaskExecutionContext, List)}, with a custom timeout. The
     * savepoint command uses a much longer timeout than the cancel command.
     */
    public static boolean executeCommand(TaskExecutionContext taskExecutionContext, List<String> args,
                                         int timeoutSeconds) {
        return executeCommandWithOutput(taskExecutionContext, args, timeoutSeconds).isSuccess();
    }

    /**
     * Same as {@link #executeCommand(TaskExecutionContext, List, int)}, and keep the output of the
     * command so that the caller can classify a failure.
     */
    public static FlinkCommandResult executeCommandWithOutput(TaskExecutionContext taskExecutionContext,
                                                              List<String> args, int timeoutSeconds) {
        return executeCommandWithOutput(taskExecutionContext, args, OSUtils.isSudoEnable(), timeoutSeconds);
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
        return executeCommandWithOutput(taskExecutionContext, args, sudoEnable, timeoutSeconds).isSuccess();
    }

    /**
     * Execute a flink command and keep its output, so that the caller can tell a command which failed
     * because the job is already in a terminal state from a real failure.
     */
    static FlinkCommandResult executeCommandWithOutput(TaskExecutionContext taskExecutionContext, List<String> args,
                                                       boolean sudoEnable, int timeoutSeconds) {
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
            return waitForCommand(process, taskExecutionContext, timeoutSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Execute flink command interrupted, taskInstanceId: {}",
                    taskExecutionContext.getTaskInstanceId(), e);
            return FlinkCommandResult.failure("");
        } catch (Exception e) {
            log.error("Execute flink command error, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
            return FlinkCommandResult.failure("");
        } finally {
            if (process != null && process.isAlive()) {
                destroyProcessTree(process, taskExecutionContext);
            }
        }
    }

    /**
     * Wait for the command to finish, consuming its output so that the command cannot block on a full
     * pipe, and keep the output for diagnostics. The command arguments are not logged, they may carry
     * sensitive configuration values.
     */
    private static FlinkCommandResult waitForCommand(Process process,
                                                     TaskExecutionContext taskExecutionContext,
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
            destroyProcessTree(process, taskExecutionContext);
            log.error("Execute flink command timeout after {}s, output: {}", timeoutSeconds, output);
            return FlinkCommandResult.failure(output.toString());
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Execute flink command failed, exitCode: {}, output: {}", exitCode, output);
            return FlinkCommandResult.failure(output.toString());
        }
        log.info("Execute flink command successfully, output: {}", output);
        return FlinkCommandResult.success(output.toString());
    }

    /**
     * The result of executing a flink command: whether it succeeded and what it printed. The output is
     * kept because a failed command has to be classified by the caller, for example cancelling a job
     * which is already in a terminal state is not a real failure.
     */
    @Getter
    public static class FlinkCommandResult {

        private final boolean success;

        private final String output;

        private FlinkCommandResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }

        public static FlinkCommandResult success(String output) {
            return new FlinkCommandResult(true, output);
        }

        public static FlinkCommandResult failure(String output) {
            return new FlinkCommandResult(false, output);
        }
    }

    /**
     * Terminate the process tree of the command and wait for it to be gone.
     *
     * <p>The process returned by the shell interceptor is the bootstrap shell (or the sudo process),
     * the flink CLI itself runs as its descendant, so killing only that process would leave the CLI
     * running and holding the output pipe.
     */
    private static void destroyProcessTree(Process process, TaskExecutionContext taskExecutionContext) {
        int processId = ProcessUtils.getProcessId(process);
        if (processId > 0) {
            ProcessUtils.kill(processId, taskExecutionContext.getTenantCode());
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        try {
            if (!process.waitFor(OUTPUT_READER_JOIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("The flink command process {} did not terminate within {}s", processId,
                        OUTPUT_READER_JOIN_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

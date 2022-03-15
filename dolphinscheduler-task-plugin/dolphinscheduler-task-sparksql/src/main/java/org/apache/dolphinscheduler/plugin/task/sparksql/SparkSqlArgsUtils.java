package org.apache.dolphinscheduler.plugin.task.sparksql;

import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.apache.dolphinscheduler.plugin.task.util.ArgsUtils;

import java.util.ArrayList;
import java.util.List;

public class SparkSqlArgsUtils {
    private static final String SPARK_CLUSTER = "cluster";

    private static final String SPARK_LOCAL = "local";

    private static final String SPARK_ON_YARN = "yarn";

    private SparkSqlArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     *
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(SparkSqlParameters param) {
        List<String> args = new ArrayList<>();
        args.add(SparkSqlConstants.MASTER);

        String deployMode = StringUtils.isNotEmpty(param.getDeployMode()) ? param.getDeployMode() : SPARK_CLUSTER;
        if (!SPARK_LOCAL.equals(deployMode)) {
            args.add(SPARK_ON_YARN);
            args.add(SparkSqlConstants.DEPLOY_MODE);
        }
        args.add(deployMode);

//        ProgramType programType = param.getProgramType();
//        String mainClass = param.getMainClass();
//        if (programType != null && programType == ProgramType.SQL && StringUtils.isNotEmpty(mainClass)) {
//            args.add(SparkSqlConstants.MAIN_CLASS);
//            args.add(mainClass);
//        }

        ProgramType programType = param.getProgramType();
        String sqlStatement = param.getSqlStatement();
        String sqlFile = param.getSqlFile();
        if (programType != null && programType == ProgramType.SQL && StringUtils.isNotEmpty(sqlStatement)){
            args.add(SparkSqlConstants.SQL_STATEMENT);
            args.add(sqlStatement);
        }else if (programType != null && programType == ProgramType.SQL && StringUtils.isNotEmpty(sqlFile)){
            args.add(SparkSqlConstants.SQL_FILE);
            args.add(sqlFile);
        }

        int driverCores = param.getDriverCores();
        if (driverCores > 0) {
            args.add(SparkSqlConstants.DRIVER_CORES);
            args.add(String.format("%d", driverCores));
        }

        String driverMemory = param.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(SparkSqlConstants.DRIVER_MEMORY);
            args.add(driverMemory);
        }

        int numExecutors = param.getNumExecutors();
        if (numExecutors > 0) {
            args.add(SparkSqlConstants.NUM_EXECUTORS);
            args.add(String.format("%d", numExecutors));
        }

        int executorCores = param.getExecutorCores();
        if (executorCores > 0) {
            args.add(SparkSqlConstants.EXECUTOR_CORES);
            args.add(String.format("%d", executorCores));
        }

        String executorMemory = param.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(SparkSqlConstants.EXECUTOR_MEMORY);
            args.add(executorMemory);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(SparkSqlConstants.SPARK_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        String others = param.getOthers();
        if (!SPARK_LOCAL.equals(deployMode) && (StringUtils.isEmpty(others) || !others.contains(SparkSqlConstants.SPARK_QUEUE))) {
            String queue = param.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(SparkSqlConstants.SPARK_QUEUE);
                args.add(queue);
            }
        }

        // --conf --files --jars --packages
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }
}

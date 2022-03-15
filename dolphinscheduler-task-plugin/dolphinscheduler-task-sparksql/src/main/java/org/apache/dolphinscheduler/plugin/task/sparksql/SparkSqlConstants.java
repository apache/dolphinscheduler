package org.apache.dolphinscheduler.plugin.task.sparksql;

public class SparkSqlConstants {
    private SparkSqlConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * --class CLASS_NAME
     */
    public static final String MAIN_CLASS = "--class";

    /**
     * --name NAME
     */
    public static final String SPARK_NAME = "--name";

    /**
     * --queue QUEUE
     */
    public static final String SPARK_QUEUE = "--queue";

    public static final String DEPLOY_MODE = "--deploy-mode";

    /**
     * --driver-cores NUM
     */
    public static final String DRIVER_CORES = "--driver-cores";

    /**
     * --driver-memory MEM
     */
    public static final String DRIVER_MEMORY = "--driver-memory";

    /**
     * master
     */
    public static final String MASTER = "--master";

    /**
     * --num-executors NUM
     */
    public static final String NUM_EXECUTORS = "--num-executors";

    /**
     * --executor-cores NUM
     */
    public static final String EXECUTOR_CORES = "--executor-cores";

    /**
     * --executor-memory MEM
     */
    public static final String EXECUTOR_MEMORY = "--executor-memory";

    /**
     * -e SQL
     */
    public static final String SQL_STATEMENT = "-e";

    /**
     * -f SQL File
     */
    public static final String SQL_FILE = "-f";

}

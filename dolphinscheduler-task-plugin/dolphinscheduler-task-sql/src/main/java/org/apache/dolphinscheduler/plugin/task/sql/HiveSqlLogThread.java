package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.hive.jdbc.HiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * hive log listener thread
 */
public class HiveSqlLogThread extends Thread{
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveSqlLogThread.class);
    /**
     * hive statement
     */
    private HiveStatement statement;
    /**
     * logger
     */
    private Logger logger;

    private TaskExecutionContext taskExecutionContext;

    public HiveSqlLogThread(Statement statement, Logger logger, TaskExecutionContext taskExecutionContext){
        this.statement = (HiveStatement) statement;
        this.logger = logger;
        this.taskExecutionContext = taskExecutionContext;
    }
    @Override
    public void run() {
        if (statement == null){
            LOGGER.info("hive statement is null,end this log query!");
        }
        try {
            while (!statement.isClosed() && statement.hasMoreLogs()){
                for (String log: statement.getQueryLog(true,500)){

                    //hive mapreduce log
                    logger.info(log);

                    List<String> appIds = LoggerUtils.getAppIds(log, logger);


                    //get sql task yarn's application_id
                    if (!appIds.isEmpty()){
                        taskExecutionContext.setAppIds(String.join(",", appIds));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Failed to view hive log,exception:[{}]",e.getMessage());
        }
    }
}

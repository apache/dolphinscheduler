package org.apache.dolphinscheduler.plugin.task.sql.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class SqlExecutionEngine implements ISqlExecutionEngine {

    protected SqlParameters sqlParameters;

    protected volatile Connection connection;

    // If set true, means the engine is canceled
    // todo: 使用状态机来控制连接初始化和close
    protected volatile boolean cancelFlag = false;

    public SqlExecutionEngine(SqlParameters sqlParameters) {
        this.sqlParameters = sqlParameters;
        this.connection = connect();
    }

    @Override
    public void execute(String sql) {
        // 1. SQL拆分

        // 2. SQL参数替换

        // 3. 前置执行执行

        // 4. 后置执行
    }

    protected Connection connect() {
        // todo: 初始化连接
        return null;
    }

    @Override
    public void close() throws SQLException {
        try (Connection connection1 = connection) {
            log.info("Closed SqlTaskEngine.");
        }
    }

}

package org.apache.dolphinscheduler.plugin.task.sql.executor;

import java.sql.SQLException;

public interface ISqlExecutionEngine extends AutoCloseable {

    /**
     * Execute the given sql, the provided sql might contain multiple lines.
     */
    void execute(String sql);

    /**
     * Close the engine, once the Engine is closed, then it cannot execute again.
     * <p> And if the Engine is executing sql, will cancel the execution.
     */
    @Override
    void close() throws SQLException;

}

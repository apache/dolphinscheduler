package org.apache.dolphinscheduler.server.worker.task.sqoop;

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTask;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.slf4j.Logger;

/**
 * sqoop task extends the shell task
 */
public class SqoopTask extends ShellTask {

    public SqoopTask(TaskProps taskProps, Logger logger){
        super(taskProps,logger);
        // get sqoopParameters
        SqoopParameters sqoopParameters =
                JSON.parseObject(taskProps.getTaskParams(),SqoopParameters.class);
        //get sqoop scripts
        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters);
        logger.info("sqoop script: {}", script);

        //set the sqoop scripts into shell parameters
        ShellParameters shellParameters = new ShellParameters();
        shellParameters.setRawScript(script);

        this.taskProps.setTaskParams(JSON.toJSONString(shellParameters));
    }
}

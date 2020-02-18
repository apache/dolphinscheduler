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

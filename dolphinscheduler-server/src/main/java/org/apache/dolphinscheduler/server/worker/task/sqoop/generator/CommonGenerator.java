package org.apache.dolphinscheduler.server.worker.task.sqoop.generator;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common script generator
 */
public class CommonGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder result = new StringBuilder();
        try{
            result.append("sqoop ")
                    .append(sqoopParameters.getModelType());
            if(sqoopParameters.getConcurrency() >0){
                result.append(" -m ")
                        .append(sqoopParameters.getConcurrency());
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}

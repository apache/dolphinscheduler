package org.apache.dolphinscheduler.server.worker.task.sqoop.generator;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HdfsSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HiveSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.MysqlSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HdfsTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HiveTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.MysqlTargetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sqoop Job Scripts Generator
 */
public class SqoopJobGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String MYSQL = "MYSQL";
    private static final String HIVE = "HIVE";
    private static final String HDFS = "HDFS";

    private ITargetGenerator targetGenerator;
    private ISourceGenerator sourceGenerator;
    private CommonGenerator commonGenerator;

    public SqoopJobGenerator(){
        commonGenerator = new CommonGenerator();
    }

    private void createSqoopJobGenerator(String sourceType,String targetType){
        this.sourceGenerator = createSourceGenerator(sourceType);
        this.targetGenerator = createTargetGenerator(targetType);
    }

    /**
     * get the final sqoop scripts
     * @param sqoopParameters
     * @return
     */
    public String generateSqoopJob(SqoopParameters sqoopParameters){
        createSqoopJobGenerator(sqoopParameters.getSourceType(),sqoopParameters.getTargetType());
        return this.commonGenerator.generate(sqoopParameters)
                + this.sourceGenerator.generate(sqoopParameters)
                + this.targetGenerator.generate(sqoopParameters);
    }

    /**
     * get the source generator
     * @param sourceType
     * @return
     */
    private ISourceGenerator createSourceGenerator(String sourceType){
        switch (sourceType){
            case MYSQL:
                return new MysqlSourceGenerator();
            case HIVE:
                return new HiveSourceGenerator();
            case HDFS:
                return new HdfsSourceGenerator();
            default:
                return null;
        }
    }

    /**
     * get the target generator
     * @param targetType
     * @return
     */
    private ITargetGenerator createTargetGenerator(String targetType){
        switch (targetType){
            case MYSQL:
                return new MysqlTargetGenerator();
            case HIVE:
                return new HiveTargetGenerator();
            case HDFS:
                return new HdfsTargetGenerator();
            default:
                return null;
        }
    }
}

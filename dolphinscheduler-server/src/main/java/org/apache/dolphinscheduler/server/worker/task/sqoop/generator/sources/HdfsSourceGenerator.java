package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHdfsParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs source generator
 */
public class HdfsSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder result = new StringBuilder();
        try{
            SourceHdfsParameter sourceHdfsParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceHdfsParameter.class);

            if(sourceHdfsParameter != null){
                if(StringUtils.isNotEmpty(sourceHdfsParameter.getExportDir())){
                    result.append(" --export-dir ")
                            .append(sourceHdfsParameter.getExportDir());
                }else{
                    throw new Exception("--export-dir is null");
                }

            }
        }catch (Exception e){
            logger.error("get hdfs source failed",e);
        }

        return result.toString();
    }
}

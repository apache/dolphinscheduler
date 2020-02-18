package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHdfsParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs target generator
 */
public class HdfsTargetGenerator implements ITargetGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder result = new StringBuilder();
        try{
            TargetHdfsParameter targetHdfsParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(),TargetHdfsParameter.class);

            if(targetHdfsParameter != null){

                if(StringUtils.isNotEmpty(targetHdfsParameter.getTargetPath())){
                    result.append(" --target-dir ").append(targetHdfsParameter.getTargetPath());
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getCompressionCodec())){
                    result.append(" --compression-codec ").append(targetHdfsParameter.getCompressionCodec());
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getFileType())){
                    result.append(" ").append(targetHdfsParameter.getFileType());
                }

                if(targetHdfsParameter.isDeleteTargetDir()){
                    result.append(" --delete-target-dir");
                }

                result.append(" --null-non-string 'NULL' --null-string 'NULL'");
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}

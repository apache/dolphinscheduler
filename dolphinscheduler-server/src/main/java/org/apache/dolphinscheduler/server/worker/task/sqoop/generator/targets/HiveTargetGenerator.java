package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive target generator
 */
public class HiveTargetGenerator implements ITargetGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters) {

        StringBuilder result = new StringBuilder();

        try{
            TargetHiveParameter targetHiveParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(),TargetHiveParameter.class);
            if(targetHiveParameter != null){

                result.append(" --hive-import ");

                if(StringUtils.isNotEmpty(targetHiveParameter.getHiveDatabase())&&
                        StringUtils.isNotEmpty(targetHiveParameter.getHiveTable())){
                    result.append(" --hive-table ")
                            .append(targetHiveParameter.getHiveDatabase())
                            .append(".")
                            .append(targetHiveParameter.getHiveTable());
                }

                if(targetHiveParameter.isCreateHiveTable()){
                    result.append(" --create-hive-table");
                }

                if(targetHiveParameter.isDropDelimiter()){
                    result.append(" --hive-drop-import-delims");
                }

                if(targetHiveParameter.isHiveOverWrite()){
                    result.append(" --hive-overwrite -delete-target-dir");
                }

                if(StringUtils.isNotEmpty(targetHiveParameter.getReplaceDelimiter())){
                    result.append(" --hive-delims-replacement ").append(targetHiveParameter.getReplaceDelimiter());
                }

                if(StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionKey())&&
                        StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionValue())){
                    result.append(" --hive-partition-key ")
                            .append(targetHiveParameter.getHivePartitionKey())
                            .append(" --hive-partition-value ")
                            .append(targetHiveParameter.getHivePartitionValue());
                }

            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}

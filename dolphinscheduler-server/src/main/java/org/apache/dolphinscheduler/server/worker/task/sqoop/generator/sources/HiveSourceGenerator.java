package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive source generator
 */
public class HiveSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder sb = new StringBuilder();
        SourceHiveParameter sourceHiveParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceHiveParameter.class);
        if(sourceHiveParameter != null){
            if(StringUtils.isNotEmpty(sourceHiveParameter.getHiveDatabase())){
                sb.append(" --hcatalog-database ").append(sourceHiveParameter.getHiveDatabase());
            }

            if(StringUtils.isNotEmpty(sourceHiveParameter.getHiveTable())){
                sb.append(" --hcatalog-table ").append(sourceHiveParameter.getHiveTable());
            }

            if(StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionKey())&&
                    StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionValue())){
                sb.append(" --hcatalog-partition-keys ").append(sourceHiveParameter.getHivePartitionKey())
                        .append(" --hcatalog-partition-values ").append(sourceHiveParameter.getHivePartitionValue());
            }
        }
        return sb.toString();
    }
}

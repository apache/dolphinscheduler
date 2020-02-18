package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.enums.QueryType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * mysql source generator
 */
public class MysqlSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder result = new StringBuilder();
        try {
            SourceMysqlParameter sourceMysqlParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceMysqlParameter.class);

            if(sourceMysqlParameter != null){
                ProcessService processDao = SpringApplicationContext.getBean(ProcessService.class);
                DataSource dataSource= processDao.findDataSourceById(sourceMysqlParameter.getSrcDatasource());
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(dataSource.getType(),
                        dataSource.getConnectionParams());
                if(baseDataSource != null){
                    result.append(" --connect ")
                            .append(baseDataSource.getJdbcUrl())
                            .append(" --username ")
                            .append(baseDataSource.getUser())
                            .append(" --password ")
                            .append(baseDataSource.getPassword());

                    if(sourceMysqlParameter.getSrcQueryType() == QueryType.FORM.ordinal()){
                        if(StringUtils.isNotEmpty(sourceMysqlParameter.getSrcTable())){
                            result.append(" --table ").append(sourceMysqlParameter.getSrcTable());
                        }

                        if(StringUtils.isNotEmpty(sourceMysqlParameter.getSrcColumns())){
                            result.append(" --columns ").append(sourceMysqlParameter.getSrcColumns());
                        }

                        StringBuilder srcWhere = new StringBuilder();
                        if(sourceMysqlParameter.getSrcConditionList().size()>0){
                            List<Property> items = sourceMysqlParameter.getSrcConditionList();
                            for(int i=0;i<items.size();i++){
                                if(i>0){
                                    srcWhere.append(" ")
                                            .append(items.get(i).getProp())
                                            .append(" ")
                                            .append(items.get(i).getValue());
                                }
                            }

                            result.append(" --where \'"+srcWhere.toString()+"\'");
                        }

                    }else if(sourceMysqlParameter.getSrcQueryType() == QueryType.SQL.ordinal()){
                        if(StringUtils.isNotEmpty(sourceMysqlParameter.getSrcQuerySql())){

                            String srcQuery = sourceMysqlParameter.getSrcQuerySql();
                            if(srcQuery.toLowerCase().contains("where")){
                                srcQuery += " AND "+"$CONDITIONS";
                            }else{
                                srcQuery += " WHERE $CONDITIONS";
                            }
                            result.append(" --query \'"+srcQuery+"\'");
                        }
                    }

                    List<Property>  mapColumnHive = sourceMysqlParameter.getMapColumnHive();

                    if(mapColumnHive != null && mapColumnHive.size() >0){
                        String columnMap = "";
                        for(Property item:mapColumnHive){
                            columnMap = item.getProp()+"="+ item.getValue()+",";
                        }

                        if(StringUtils.isNotEmpty(columnMap)){
                            result.append(" --map-column-hive ")
                                    .append(columnMap.substring(0,columnMap.length()-1));
                        }
                    }

                    List<Property>  mapColumnJava = sourceMysqlParameter.getMapColumnJava();

                    if(mapColumnJava != null && mapColumnJava.size() >0){
                        String columnMap = "";
                        for(Property item:mapColumnJava){
                            columnMap = item.getProp()+"="+ item.getValue()+",";
                        }

                        if(StringUtils.isNotEmpty(columnMap)){
                            result.append(" --map-column-java ")
                                    .append(columnMap.substring(0,columnMap.length()-1));
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}

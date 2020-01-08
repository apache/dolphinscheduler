package org.apache.dolphinscheduler.api.utils.exportprocess;

/**
 * @ClassName DataSourceParam
 */
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName DataSourceParam
 */
public class DataSourceParam implements exportProcessAddTaskParam, InitializingBean {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    public JSONObject addSpecialParam(JSONObject taskNode) {
        // add sqlParameters
        JSONObject sqlParameters = JSONUtils.parseObject(taskNode.getString("params"));
        DataSource dataSource = dataSourceMapper.selectById((Integer) sqlParameters.get("datasource"));
        if (null != dataSource) {
            sqlParameters.put("datasourceName", dataSource.getName());
        }
        taskNode.put("params", sqlParameters);

        return taskNode;
    }


    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.SQL.name(), this);
        TaskNodeParamFactory.register(TaskType.PROCEDURE.name(), this);
    }
}
package org.apache.dolphinscheduler.server.worker.task.flinkx;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.flinkx.FlinkxParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.FlinkxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.FlinkxUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlinkxTask extends AbstractYarnTask {

    private static final String FLINKX_COMMAND = "flinkx";

    /**
     * flinkx mode
     */
    private static final String FLINKX_MODE_PARAM = "-mode";

    /**
     * flinkx job
     */
    private static final String FLINKX_JOB_ID = "-jobid";

    /**
     * flinkx job
     */
    private static final String FLINKX_JOB_PARAM = "-job";

    /**
     * flink conf
     */
    private static final String FLINKX_CONF_PARAM = "-flinkconf";

    /**
     * TODO
     * flink cluster conf
     */
    private static final String FLINKX_LOCAL_PATH = "${FLINKX_HOME}/flinkconf";

    /**
     *
     * flink cluster conf
     */

    private static final String FLINK_CLUSTER_PATH = "${FLINK_HOME}/conf";

    /**
     * flinkx plugin param
     */
    private static final String FLINKX_PLUGIN_PARAM = "-pluginRoot";

    /**
     * flinkx plugin path
     */
    private static final String FLINKX_PLUGIN_PATH = "${FLINKX_HOME}/syncplugins";

    /**
     * flinkx channel count
     */
    private static final int FLINKX_CHANNEL_COUNT = 1;

    private static final String FLINKX_JOB_MAGIC_NAME = "flinkx_job_%s";

    /**
     *  flinkx parameters
     */
    private FlinkxParameters flinkxParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;


    private String flinkxJobId;


    public FlinkxTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() throws Exception {
        logger.info("flinkx task params {}", taskExecutionContext.getTaskParams());
        flinkxParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkxParameters.class);
        flinkxJobId = String.format(FLINKX_JOB_MAGIC_NAME,taskExecutionContext.getTaskAppId());
        //check flinkx task params
        if (null == flinkxParameters) {
            throw new IllegalArgumentException("Flinkx Task params is null");
        }

        if (!flinkxParameters.checkParameters()) {
            throw new RuntimeException("flinkx task params is not valid");
        }
    }

    @Override
    protected String buildCommand() throws Exception {

        // combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                taskExecutionContext.getDefinedParams(),
                flinkxParameters.getLocalParametersMap(),
                CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                taskExecutionContext.getScheduleTime());

        // run flinkx process
        String jsonFilePath = buildFlinkxJsonFile(paramsMap);

        // flinkx -mode <local> -job <job path> -flinkconf <flinkconf> -pluginRoot(fixed) <syncplugins>
        List<String> args = new ArrayList<>();
        args.add(FLINKX_COMMAND);

        args.add(FLINKX_MODE_PARAM);
        args.add(flinkxParameters.getMode());

        args.add(FLINKX_JOB_ID);
        args.add(flinkxJobId);

        args.add(FLINKX_JOB_PARAM);
        args.add(jsonFilePath);

        //-flinkconf    flink standalone mode
        if(FlinkxMode.local.name().equalsIgnoreCase(flinkxParameters.getMode())){
            args.add(FLINKX_CONF_PARAM);
            args.add(FLINKX_LOCAL_PATH);
        }else {
            args.add(FLINKX_CONF_PARAM);
            args.add(FLINK_CLUSTER_PATH);
        }

        args.add(FLINKX_PLUGIN_PARAM);
        args.add(FLINKX_PLUGIN_PATH);

        String command = ParameterUtils
                .convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());

        logger.info("flinkx task command : {}", command);

        return command;
    }

    /**
     * build flinkx configuration file
     *
     * @return flinkx json file name
     * @throws Exception if error throws Exception
     */
    private String buildFlinkxJsonFile(Map<String, Property> paramsMap) throws Exception{
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (flinkxParameters.getCustomConfig() == Flag.YES.ordinal()){
            json = flinkxParameters.getJson().replaceAll("\\r\\n", "\n");
        }else {
            JSONObject job = new JSONObject();
            job.put("content", buildFlinkxJobContentJson());
            job.put("setting", buildFlinkxJobSettingJson());

            JSONObject root = new JSONObject();
            root.put("job", job);
            json = root.toString();
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));

        logger.debug("flinkx job json : {}", json);

        // create flinkx json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * build flinkx job config
     *
     * @return collection of flinkx job config JSONObject
     * @throws SQLException if error throws SQLException
     */
    private Object buildFlinkxJobContentJson() throws SQLException{
        FlinkxTaskExecutionContext flinkxTaskExecutionContext = taskExecutionContext.getFlinkxTaskExecutionContext();
        DbType sourceDBType = DbType.of(flinkxTaskExecutionContext.getSourcetype());
        DbType targetDBType = DbType.of(flinkxTaskExecutionContext.getTargetType());

        BaseDataSource dataSourceCfg = DataSourceFactory.getDatasource(sourceDBType,
                flinkxTaskExecutionContext.getSourceConnectionParams());

        BaseDataSource dataTargetCfg = DataSourceFactory.getDatasource(targetDBType,
                flinkxTaskExecutionContext.getTargetConnectionParams());
        //reader
        List<JSONObject> readerConnArr = new ArrayList<>();
        JSONObject readerConn = new JSONObject();
        readerConn.put("jdbcUrl",new String[]{dataSourceCfg.getJdbcUrl()});
        readerConn.put("table",new String[]{flinkxParameters.getSourceTable()});
        readerConnArr.add(readerConn);

        JSONObject readerParam = new JSONObject();
        readerParam.put("username",dataSourceCfg.getUser());
        readerParam.put("password",dataSourceCfg.getPassword());
        readerParam.put("connection",readerConnArr);
        readerParam.put("column",flinkxParameters.getSourceColumns());

        if(flinkxParameters.isSqlStatement()){
            readerParam.put("customSql",flinkxParameters.getCustomSql());
        }

        if(flinkxParameters.isPolling()){
            readerParam.put("polling",flinkxParameters.isPolling());
            readerParam.put("pollingInterval",flinkxParameters.getPollingInterval());
            readerParam.put("increColumn",flinkxParameters.getIncreColumn());
            if(StringUtils.isNotEmpty(flinkxParameters.getStartLocation())){
                readerParam.put("startLocation",flinkxParameters.getStartLocation());
            }
        }

        if(flinkxParameters.isSplit() && flinkxParameters.getJobSpeedChannel() > 1){
            readerParam.put("splitPk",flinkxParameters.getSplitPk());
        }

        JSONObject reader = new JSONObject();
        reader.put("name", FlinkxUtils.getReaderPluginName(sourceDBType));
        reader.put("parameter",readerParam);

        //writer
        List<JSONObject> writerConnArr = new ArrayList<>();
        JSONObject writerConn = new JSONObject();
        writerConn.put("jdbcUrl",new String[]{dataTargetCfg.getJdbcUrl()});
        writerConn.put("table",new String[]{flinkxParameters.getTargetTable()});
        writerConnArr.add(writerConn);

        JSONObject writerParam = new JSONObject();
        writerParam.put("username",dataTargetCfg.getUser());
        writerParam.put("password",dataTargetCfg.getPassword());
        writerParam.put("connection",writerConnArr);
        writerParam.put("column",flinkxParameters.getTargetColumns());
        writerParam.put("writeMode",flinkxParameters.getWriteMode());
        WriteMode writeMode = flinkxParameters.getWriteMode();
        if(writeMode!=null && writeMode == WriteMode.INSERT){
            if(targetDBType !=null && targetDBType == DbType.POSTGRESQL){
                writerParam.put("insertSqlMode","copy");
            }
        }

        if(writeMode!=null && writeMode == WriteMode.UPDATE){
            //唯一索引
            JSONObject updateKey = new JSONObject();
            updateKey.put("key",flinkxParameters.getUniqueKey());
            writerParam.put("updateKey",updateKey);
        }

        if(CollectionUtils.isNotEmpty(flinkxParameters.getPreStatements())){
            writerParam.put("preSql",flinkxParameters.getPreStatements());
        }

        if(CollectionUtils.isNotEmpty(flinkxParameters.getPostStatements())){
            writerParam.put("postSql",flinkxParameters.getPostStatements());
        }

        JSONObject writer = new JSONObject();
        writer.put("name", FlinkxUtils.getWriterPluginName(DbType.of(flinkxTaskExecutionContext.getTargetType())));
        writer.put("parameter",writerParam);

        List<JSONObject> contentList = new ArrayList<>();
        JSONObject content = new JSONObject();
        content.put("reader", reader);
        content.put("writer", writer);
        contentList.add(content);

        return contentList;

    }

    /**
     * build flinkx setting config
     *
     * @return flinkx setting config JSONObject
     */
    private Object buildFlinkxJobSettingJson() {

        JSONObject speed = new JSONObject();

        if(flinkxParameters.isSplit() && flinkxParameters.getJobSpeedChannel() > 1){
            speed.put("channel",flinkxParameters.getJobSpeedChannel());
        }else {
            speed.put("channel", FLINKX_CHANNEL_COUNT);
        }

        if (flinkxParameters.getJobSpeedByte() > 0) {
            speed.put("byte", flinkxParameters.getJobSpeedByte());
        }

        JSONObject errorLimit = new JSONObject();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0.0D);

        JSONObject restore = new JSONObject();
        restore.put("isStream",flinkxParameters.isStream());
        restore.put("isRestore",flinkxParameters.isRestore());
        if(flinkxParameters.isRestore()){
            restore.put("restoreColumnName",flinkxParameters.getRestoreColumnName());
            restore.put("restoreColumnIndex",flinkxParameters.getRestoreColumnIndex());
        }
        restore.put("maxRowNumForCheckpoint",flinkxParameters.getMaxRowNumForCheckpoint());

//        JSONObject dirty = new JSONObject();
//        JSONObject log = new JSONObject();

        JSONObject setting = new JSONObject();
        setting.put("speed", speed);
        setting.put("errorLimit", errorLimit);
        setting.put("restore", restore);
//        setting.put("dirty",dirty);
//        setting.put("log", log);

        return setting;

    }


    @Override
    public void cancelApplication(boolean status) throws Exception {
        super.cancelApplication(status);
        //cancel flink application
    }

    /**
     * flinkx invalid
     */
    @Override
    protected void setMainJarName() {}

    @Override
    public AbstractParameters getParameters() {
        return flinkxParameters;
    }
}

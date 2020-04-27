package org.apache.dolphinscheduler.server.worker.task.connectors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.connectors.ConnectorsParameters;
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

public class ConnectorsTask extends AbstractTask {

    private ConnectorsParameters connectorsParameters ;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private volatile boolean done ;

    /**
     * init Connectors config
     */
    @Override
    public void init() {
        logger.info("connectors task params {}", taskExecutionContext.getTaskParams());
        connectorsParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ConnectorsParameters.class);
        if (!connectorsParameters.checkParameters()) {
            throw new RuntimeException("connectors task params is not valid");
        }
    }


    /**
     * constructor
     *
     * @param taskExecutionContext
     *         taskExecutionContext
     * @param logger
     *         logger
     */
    protected ConnectorsTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    /**
     * cancel Connectors process
     *
     * @param cancelApplication cancelApplication
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancelApplication(boolean cancelApplication)
            throws Exception {
        super.cancelApplication(cancelApplication);
        HttpUtils.request("DELETE",connectorsParameters.getKillUrl(),null,null,null) ;
    }

    @Override
    public void handle() throws Exception {
        //任务初始化状态
        done= false ;

        String result  = null;

        //构建请求参数
        Map<String,Object> map = new HashMap();

        map.put("name", connectorsParameters.getName() );

        map.put("config", JSONUtils.parseObject(connectorsParameters.getConfig()) );

        //构建参数
        String bodyParams = JSONUtils.toJson(map);

        //创建任务
        result = HttpUtils.request("POST",connectorsParameters.getAddUrl(),null,null,bodyParams);

        if(null != result && !StringUtils.isBlank(result)){
            Map<String,Object> res = JSONUtils.parseObject(result,Map.class);
            if(null != res.get("error_code")){
                throw  new Exception(result);
            }
        }

        //轮询任务



        while (!done){

            //创建任务
            result = HttpUtils.request("GET",connectorsParameters.getStatusUrl(),null,null,bodyParams);

            if(null != result && !StringUtils.isBlank(result)){
                Map<String,Object> res = JSONUtils.parseObject(result,Map.class);

                if(null == res.get("status") || "FAILED".equals(res.get("status")) ){
                    done = true ;
                    this.cancelApplication(true);

                    //获取错误详情
                    String errorInfo = HttpUtils.request("GET",connectorsParameters.getErrorUrl(),null,null,null);

                    throw  new Exception(errorInfo);
                }

                if( "DESTROYED".equals(res.get("status"))){
                    done = true ;
                    logger.warn("this connectorsTask [ {} ]  is destroyed . " ,connectorsParameters.getName() );
                }

                if( "FINISH".equals(res.get("status"))){
                    done = true ;
                    logger.info("this connectorsTask [ {} ]  is finished . " ,connectorsParameters.getName() );
                }
            }

            //每秒轮训任务状态
            Thread.sleep(1000);
        }


    }

    @Override
    public AbstractParameters getParameters() {
        return this.connectorsParameters;
    }


}
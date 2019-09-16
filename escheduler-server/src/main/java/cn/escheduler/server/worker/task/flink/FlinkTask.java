package cn.escheduler.server.worker.task.flink;

import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.flink.FlinkParameters;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.server.utils.FlinkArgsUtils;
import cn.escheduler.server.worker.task.AbstractYarnTask;
import cn.escheduler.server.worker.task.TaskProps;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lihongyu@hualala.com
 * @date 2019/5/17 2:54 PM
 */
public class FlinkTask extends AbstractYarnTask {
    /**
     *  flink parameters
     */
    private FlinkParameters flinkParameters;

    private String FLINK_COMMAND = "flink";


    public FlinkTask(TaskProps taskProps, Logger logger){
        super(taskProps, logger);

    }
    @Override
    public void init() {
        logger.info("flink task params {}", taskProps.getTaskParams());
        flinkParameters = JSONUtils.parseObject(taskProps.getTaskParams(), FlinkParameters.class);

        if (!flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }
    }

    @Override
    protected String buildCommand() throws Exception {
        List<String> args = new ArrayList<>();

        args.add(FLINK_COMMAND);

        // other parameters
        args.addAll(FlinkArgsUtils.buildArgs(flinkParameters));

        StringBuilder sb = new StringBuilder(String.join(" ", args));

        // add flink -- [ARGUMENTS]
        if(flinkParameters.getLocalParametersMap() != null) {
            flinkParameters.getLocalParametersMap().forEach((k,v)->{
                sb.append(" --").append(k).append(" ").append(v.getValue());
            });
        }

        String command = sb.toString();
        logger.info("flink task command : {}", command);

        return command;
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }
}

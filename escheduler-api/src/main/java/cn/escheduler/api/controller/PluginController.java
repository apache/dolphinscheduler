package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.common.plugin.PluginManager;
import cn.escheduler.common.plugin.StageDisplayInfo;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.escheduler.api.enums.Status.PLUGIN_LOAD_PLUGIN_LIST_ERROR;

/**
 * sdc controller
 */
@RestController
@RequestMapping("plugin")
public class PluginController extends  BaseController {
    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);

    /**
     * get all stage list
     * @param loginUser
     * @return
     */
    @GetMapping(value = "/list-stages")
    @ResponseStatus(HttpStatus.OK)
    public Result listStages(@RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        try{
            Map<String, Object> result = new HashMap<>(5);
            List<StageDisplayInfo> stages = PluginManager.getInstance().getAllStages();
            result.put(Constants.DATA_LIST, stages);
            putMsg(result, Status.SUCCESS);

            return returnDataList(result);
        } catch (Exception e){
            logger.error(PLUGIN_LOAD_PLUGIN_LIST_ERROR.getMsg(),e);
            return error(PLUGIN_LOAD_PLUGIN_LIST_ERROR.getCode(),
                    PLUGIN_LOAD_PLUGIN_LIST_ERROR.getMsg());
        }
    }
}
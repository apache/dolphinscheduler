package org.apache.dolphinscheduler.api.utils.exportprocess;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName exportProcessAddTaskParam
 */
public interface exportProcessAddTaskParam {
    /**
     * add task special param: sql task dependent task
     */
    JSONObject addSpecialParam(JSONObject taskNode);
}

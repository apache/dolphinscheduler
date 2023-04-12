package org.apache.dolphinscheduler.api.test.pages.security;

import lombok.AllArgsConstructor;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class WorkerGroupPage {

    private String sessionId;

    public HttpResponse saveWorkerGroup(User loginUser, int id, String name, String addrList, String description, String otherParamsJson) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("id", id);
        params.put("name", name);
        params.put("addrList", addrList);
        params.put("description", description);
        params.put("otherParamsJson", otherParamsJson);

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();

        return requestClient.post("/worker-groups", headers, params);
    }

    public HttpResponse queryAllWorkerGroups(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/worker-groups/all", headers, params);
    }

}

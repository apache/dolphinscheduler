package org.apache.dolphinscheduler.common.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * request context holder
 */
public class RequestContext {

    private static final ThreadLocal<RequestAttr> requestHolder = new ThreadLocal<>();

    public static <T> void setLoginUser(T loginUser) {
        RequestAttr attr = getRequestAttr();
        attr.putObject("loginUser", loginUser);
    }

    public static <T> T getLoginUser() {
        RequestAttr attr = getRequestAttr();
        if (attr == null) {
            return null;
        }
        return requestHolder.get().getObject("loginUser");
    }

    private static RequestAttr getRequestAttr() {
        RequestAttr attr = requestHolder.get();
        if (attr == null) {
            synchronized (requestHolder) {
                attr = requestHolder.get();
                if (attr == null) {
                    attr = new RequestAttr();
                    requestHolder.set(attr);
                }
            }
        }

        return attr;
    }

    public static void clear() {
        requestHolder.get().map.clear();
    }

    public static class RequestAttr {

        private Map<String, Object> map = new HashMap<>();

        public <T> T getObject(String key) {
            return (T) map.get(key);
        }

        public void putObject(String key, Object obj) {
            map.put(key, obj);
        }

    }

}

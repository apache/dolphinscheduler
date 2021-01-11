package org.apache.dolphinscheduler.remote.rpc.filter;

import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SelectorFilter
 */
public class SelectorFilter implements Filter {


    private static final Logger logger = LoggerFactory.getLogger(SelectorFilter.class);


    private SelectorFilter selectorFilter = SelectorFilter.getInstance();

    public static SelectorFilter getInstance() {
        return SelectorFilterInner.INSTANCE;
    }


    private static class SelectorFilterInner {

        private static final SelectorFilter INSTANCE = new SelectorFilter();
    }

    private SelectorFilter() {
    }

    @Override
    public RpcResponse filter(Invoker invoker, RpcRequest req) throws Throwable {
        RpcResponse rsp = new RpcResponse();
        rsp.setMsg("ms");
        return rsp;
    }
}

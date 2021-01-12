package org.apache.dolphinscheduler.remote.rpc.filter;

import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.remote.rpc.filter.directory.Directory;
import org.apache.dolphinscheduler.remote.rpc.selector.RandomSelector;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

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
        Directory.getInstance().addServer("default","127.0.0.1:8080");
        Directory.getInstance().addServer("default","127.0.0.2:8080");
        Directory.getInstance().addServer("default","127.0.0.3:8080");
        List<String> hosts = Directory.getInstance().getDirectory("default");
        List<Host> candidateHosts = new ArrayList<>(hosts.size());
        hosts.forEach(node -> {
            Host nodeHost = Host.of(node);
            nodeHost.setWorkGroup("default");
            candidateHosts.add(nodeHost);
        });
        RandomSelector randomSelector = new RandomSelector();
        System.out.println(randomSelector.doSelect(candidateHosts));
        RpcResponse rsp = new RpcResponse();
        rsp.setMsg("ms");
        return rsp;
    }
}

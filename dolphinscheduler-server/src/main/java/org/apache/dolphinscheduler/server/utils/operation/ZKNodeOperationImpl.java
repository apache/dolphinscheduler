package org.apache.dolphinscheduler.server.utils.operation;

import org.apache.dolphinscheduler.common.zk.ZookeeperOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * zk  node operation
 */
@ComponentScan("org.apache.dolphinscheduler")
public class ZKNodeOperationImpl extends ZookeeperOperator implements NodeOperation,CommandLineRunner {

    private static Integer ARGS_LENGTH = 1;

    private static final Logger logger = LoggerFactory.getLogger(ZKNodeOperationImpl.class);

    /**
     * judge whether node exists
     * @param node register node
     * @return whether node exists
     */
    @Override
    public Boolean exists(String node) {
        try {
            return isExisted(node);
        }catch (Exception e){
            logger.error("error",e);
            return false;
        }finally {
            close();
        }
    }

    /**
     * remove node cascade
     * @param rootNode register node
     * @return remove node status
     */
    @Override
    public Boolean removeNode(String rootNode) {
        try {

            remove(rootNode);

            logger.info("delete node : {}", rootNode);

            return true;
        }catch (Exception e){
            logger.error("error",e);
            return false;
        }finally {
            close();
        }
    }

    /**
     * list nodes by path
     * @param path path
     * @return list nodes
     */
    @Override
    public List<String> listNodesByPath(String path) {
        try {
            return getChildrenKeys(path);
        }catch (Exception e){
            logger.error("error",e);
            return null;
        }finally {
            close();
        }
    }


    public static void main(String[] args) {

        new SpringApplicationBuilder(ZKNodeOperationImpl.class).web(WebApplicationType.NONE).run(args);
    }


    @Override
    public void run(String... args) throws Exception {
        if (args.length != ARGS_LENGTH){
            logger.error("Usage: <rootNode>");
            return;
        }

        removeNode(args[0]);
    }
}

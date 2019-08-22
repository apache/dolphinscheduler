package cn.escheduler.server.worker;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class EnvFileTest {

    private static  final Logger logger = LoggerFactory.getLogger(EnvFileTest.class);

    @Test
    public void test() {
        String path = System.getProperty("user.dir")+"/script/env/.escheduler_env.sh";
        String pythonHome = getPythonHome(path);
        logger.info(pythonHome);
    }

    /**
     *  get python home
     * @param path
     * @return
     */
    private static String getPythonHome(String path){
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            while ((line = br.readLine()) != null){
                if (line.contains("PYTHON_HOME")){
                    sb.append(line);
                    break;
                }
            }
            String result = sb.toString();
            if (StringUtils.isEmpty(result)){
                return null;
            }
            String[] arrs = result.split("=");
            if (arrs.length == 2){
                return arrs[1];
            }

        }catch (IOException e){
            logger.error("read file failed : " + e.getMessage(),e);
        }finally {
            try {
                if (br != null){
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return null;
    }
}

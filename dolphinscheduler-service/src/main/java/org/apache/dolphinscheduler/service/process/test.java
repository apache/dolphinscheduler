package org.apache.dolphinscheduler.service.process;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author wangxj
 * @date 2021/4/13 10:49
 */
public class test {
    public static void main(String[] args) throws ScriptException {
        String str = "(4 >= 0 && 4 <= 5)";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(str);
        System.out.println(result);
    }
}

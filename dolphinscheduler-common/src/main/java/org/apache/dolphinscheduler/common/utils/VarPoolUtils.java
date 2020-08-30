package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.TaskParams;

import java.text.ParseException;
import java.util.Map;

public class VarPoolUtils {
  /**
   * setTaskNodeLocalParams
   * @param taskNode taskNode
   * @param prop LocalParamName
   * @param value LocalParamValue
   */
  public static void setTaskNodeLocalParams(TaskNode taskNode, String prop, Object value) {
    String taskParamsJson = taskNode.getParams();
    TaskParams taskParams = JSONUtils.parseObject(taskParamsJson, TaskParams.class);
    if(taskParams == null) {
      return;
    }
    taskParams.setLocalParamValue(prop, value);
    taskNode.setParams(JSONUtils.toJsonString(taskParams));
  }
  
  /**
   * setTaskNodeLocalParams
   * @param taskNode taskNode
   * @param propToValue propToValue
   */
  public static void setTaskNodeLocalParams(TaskNode taskNode, Map<String, Object> propToValue) {
    String taskParamsJson = taskNode.getParams();
    TaskParams taskParams = JSONUtils.parseObject(taskParamsJson, TaskParams.class);
    if(taskParams == null) {
      return;
    }
    taskParams.setLocalParamValue(propToValue);
    taskNode.setParams(JSONUtils.toJsonString(taskParams));
  }
  
  /**
   * convertVarPoolToMap
   * @param propToValue propToValue
   * @param varPool varPool
   * @throws ParseException ParseException
   */
  public static void convertVarPoolToMap(Map<String, Object> propToValue, String varPool) throws ParseException {
    if (varPool == null) {
      return;
    }
    String[] splits = varPool.split("\\$guyinyou\\$");
    for (String kv : splits) {
      String[] kvs = kv.split(",");
      if (kvs.length == 2) {
        propToValue.put(kvs[0], kvs[1]);
      } else {
        throw new ParseException(kv, 2);
      }
    }
  }
  
  /**
   * convertPythonScriptPlaceholders
   * @param rawScript rawScript
   * @return String
   * @throws StringIndexOutOfBoundsException StringIndexOutOfBoundsException
   */
  public static String convertPythonScriptPlaceholders(String rawScript) throws StringIndexOutOfBoundsException {
    int len = "${setShareVar(${".length();
    int scriptStart = 0;
    while ((scriptStart = rawScript.indexOf("${setShareVar(${", scriptStart)) != -1) {
      int start = -1;
      int end = rawScript.indexOf('}', scriptStart + len);
      String prop = rawScript.substring(scriptStart + len, end);
      
      start = rawScript.indexOf(',', end);
      end = rawScript.indexOf(')', start);
      
      String value = rawScript.substring(start + 1, end);
      
      start = rawScript.indexOf('}', start) + 1;
      end = rawScript.length();
      
      String replaceScript = String.format("print(\"${{setValue({},{})}}\".format(\"%s\",%s))", prop, value);
      
      rawScript = rawScript.substring(0, scriptStart) + replaceScript + rawScript.substring(start, end);
      
      scriptStart += replaceScript.length();
    }
    return rawScript;
  }
}

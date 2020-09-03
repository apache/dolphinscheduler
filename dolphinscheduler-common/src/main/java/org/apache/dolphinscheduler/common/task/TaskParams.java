package org.apache.dolphinscheduler.common.task;

import java.util.Map;

public class TaskParams {

  private String rawScript;
  private Map<String,String>[] localParams;

  public void setRawScript(String rawScript) {
    this.rawScript = rawScript;
  }

  public void setLocalParams(Map<String,String>[] localParams) {
    this.localParams = localParams;
//    this.localParams = JSONUtils.parseObject(localParams, LocalParam[].class);
  }

  public String getRawScript() {
    return rawScript;
  }

  public void setLocalParamValue(String prop, Object value) {
    if(localParams == null) {
      return;
    }
    for (int i = 0; i < localParams.length; i++) {
      if (localParams[i].get("prop").equals(prop)) {
        localParams[i].put("value", (String)value);
      }
    }
  }

  public void setLocalParamValue(Map<String,Object> propToValue) {
    if(localParams == null) {
      return;
    }
    for (int i = 0; i < localParams.length; i++) {
      String prop = localParams[i].get("prop");
      if (propToValue.containsKey(prop)) {
        localParams[i].put("value",(String)propToValue.get(prop));
      }
    }
  }

  public Map<String,String>[] getLocalParams() {
    return localParams;
  }
} 
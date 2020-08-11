package org.apache.dolphinscheduler.common.task;

import java.util.Map;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TaskParams {
	
	private String rawScript;
	private JSONArray localParams;
	
	public void setRawScript(String rawScript) {
		this.rawScript = rawScript;
	}
	public void setLocalParams(String localParams) {
		this.localParams = JSONObject.parseArray(localParams);
	}
	
	public String getRawScript() {
		return rawScript;
	}
	public void setLocalParamValue(String prop, Object value) {
		for(int i = 0; i < localParams.size(); i++) {
			if(localParams.getJSONObject(i).getString("prop").equals(prop)) {
				localParams.getJSONObject(i).put("value", value);
			}
		}
	}
	public void setLocalParamValue(Map<String,Object> propToValue) {
		for(int i = 0; i < localParams.size(); i++) {
			String prop = localParams.getJSONObject(i).getString("prop");
			if(propToValue.containsKey(prop)) {
				localParams.getJSONObject(i).put("value", propToValue.get(prop));
			}
		}
	}
	
	public JSONArray getLocalParams() {
		return localParams;
	}
}

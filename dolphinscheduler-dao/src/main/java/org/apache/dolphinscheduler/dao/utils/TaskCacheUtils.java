package org.apache.dolphinscheduler.dao.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

public class TaskCacheUtils {

    final static String MERGE_TAG = "-";

    public static String generateCacheKey(TaskInstance taskInstance, TaskExecutionContext context) {
        List<String> keyElements = new ArrayList<>();
        keyElements.add(String.valueOf(taskInstance.getTaskCode()));
        keyElements.add(String.valueOf(taskInstance.getTaskDefinitionVersion()));
        keyElements.add(String.valueOf(taskInstance.getIsCache().getCode()));
        keyElements.add(getTaskInputVarPool(taskInstance, context));
        String data = StringUtils.join(keyElements, "_");
        String cacheKey = md5(data);
        System.out.println("cacheKey: " + cacheKey + ", data: " + data);
        return cacheKey;
    }

    public static String generateTagCacheKey(Integer sourceTaskId, String cacheKey) {
        return sourceTaskId + MERGE_TAG + cacheKey;
    }

    public static String revertCacheKey(String tagCacheKey) {
        if (tagCacheKey == null) {
            return "";
        }
        if (tagCacheKey.contains(MERGE_TAG)) {
            return tagCacheKey.split(MERGE_TAG)[1];
        } else {
            return tagCacheKey;
        }
    }

    public static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5 = md.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : md5) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTaskInputVarPool(TaskInstance taskInstance, TaskExecutionContext context) {
        JsonNode taskParams = JSONUtils.parseObject(taskInstance.getTaskParams());
        Set<String> propertyInSet = JSONUtils.toList(taskParams.get("localParams").toString(), Property.class).stream()
                .filter(property -> property.getDirect().equals(Direct.IN))
                .map(Property::getProp).collect(Collectors.toSet());

        propertyInSet.addAll(getScriptVarInSet(taskInstance));

        List<Property> varPool = JSONUtils.toList(taskInstance.getVarPool(), Property.class);

        if (context.getPrepareParamsMap() != null) {
            Set<String> taskVarPoolSet = varPool.stream().map(Property::getProp).collect(Collectors.toSet());
            List<Property> globalContextVarPool = context.getPrepareParamsMap().entrySet().stream()
                    .filter(entry -> !taskVarPoolSet.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            varPool.addAll(globalContextVarPool);
        }

        varPool = varPool.stream()
                .filter(property -> property.getDirect().equals(Direct.IN))
                .filter(property -> propertyInSet.contains(property.getProp()))
                .sorted(Comparator.comparing(Property::getProp))
                .collect(Collectors.toList());
        return JSONUtils.toJsonString(varPool);
    }

    public static List<String> getScriptVarInSet(TaskInstance taskInstance) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(taskInstance.getTaskParams());

        List<String> varInSet = new ArrayList<>();
        while (matcher.find()) {
            varInSet.add(matcher.group(1));
        }
        return varInSet;
    }

}

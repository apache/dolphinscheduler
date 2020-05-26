package org.apache.dolphinscheduler.common.utils.dependent;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;

public class DependCheckUtils {

    private static final String MARK_WORD_DEPEND = "depend";

    private static final String MARK_WORD_TARGET = "target";

    public static String clearIllegalChars(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }

        str = str.replace(" ", "");
        str = str.replace("\t", "");
        str = str.replace("\n", "");
        str = str.replace("，", ",");
        return str;
    }

    public static String removeMarkWord(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        str = str.replace("(" + MARK_WORD_DEPEND + ")", "");
        str = str.replace("(" + MARK_WORD_TARGET + ")", "");
        return str;
    }

    public static String buildDependTableUnionKey(String host, String database, String tableName) {
        return buildUnionKey(host, database, tableName, MARK_WORD_DEPEND);
    }

    public static String buildDependTableUnionKey(String host, String database, List<String> tableList) {
        return buildUnionKey(host, database, tableList, MARK_WORD_DEPEND);
    }

    public static String buildTargetTableUnionKey(String host, String database, String tableName) {
        return buildUnionKey(host, database, tableName, MARK_WORD_TARGET);
    }

    public static String buildTargetTableUnionKey(String host, String database, List<String> tableList) {
        return buildUnionKey(host, database, tableList, MARK_WORD_TARGET);
    }

    public static String buildTargetTableUnionKey(String host, String database, String[] tableArray) {
        return buildUnionKey(host, database, Arrays.asList(tableArray), MARK_WORD_TARGET);
    }

    public static String buildUnionKey(String host, String database, List<String> tableList, String markWord) {
        if (CollectionUtils.isEmpty(tableList)) {
            return null;
        }

        String[] unionKeys = new String[tableList.size()];
        for (int i = 0; i < tableList.size(); i++) {
            unionKeys[i] = buildUnionKey(host, database, tableList.get(i), markWord);
        }

        return StringUtils.join(unionKeys, Constants.COMMA);
    }

    public static String buildUnionKey(String host, String database, String tableName, String markWord) {
        if (StringUtils.isEmpty(tableName)) {
            return null;
        }

        if (tableName.indexOf(Constants.DOT) > -1) {
            return String.format("%s:%s(%s)", host, tableName.replace(Constants.DOT, Constants.COLON), markWord);
        } else {
            return String.format("%s:%s:%s(%s)", host, database, tableName, markWord);
        }
    }

    public static boolean existDependRelation(TaskNode taskNode, String[] dependNodeKeys) {
        AbstractParameters parameters = TaskParametersUtils.getParameters(taskNode.getType(), taskNode.getParams());
        return existDependRelation(parameters.getTargetNodeKeys(), dependNodeKeys);
    }

    public static boolean existDependRelation(String targetNodeKey, String[] dependNodeKeys) {
        if (StringUtils.isEmpty(targetNodeKey)) {
            return false;
        }

        for (String dependKey : dependNodeKeys) {
            if (targetNodeKey.indexOf(dependKey.replace(MARK_WORD_DEPEND, MARK_WORD_TARGET)) > -1) {
                return true;
            }
        }

        return false;
    }

    public static String[] replaceMarkWordToTarget(String[] dependNodeKeys) {
        String[] targetNodeKeys = new String[dependNodeKeys.length];
        if(dependNodeKeys != null) {
            for(int i = 0; i < dependNodeKeys.length; i++) {
                targetNodeKeys[i] = replaceMarkWordToTarget(dependNodeKeys[i]);
            }
        }
        return targetNodeKeys;
    }

    public static String replaceMarkWordToTarget(String dependNodeKey) {
        return dependNodeKey.replace(MARK_WORD_DEPEND, MARK_WORD_TARGET);
    }
}

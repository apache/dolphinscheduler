package org.apache.dolphinscheduler.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlUtils {

    private static final String SQL_SPLIT_CHAR = ";";
    private static final String SQL_NEW_LINE = "\n";

    /**
     * Split statements at semicolons ignoring the ones inside quotes and comments.
     * The comment symbols that come inside quotes should be ignored.
     *
     * @param sql string contains multi statements
     * @return list of statements
     */
    public static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        String current = "";
        char prev = '\0';
        Character betweenQuotes = null;
        Boolean isComment = null;
        int endCol = sql.length() - 1;

        // return if sql contains only 1 statement
        if (!sql.contains(SQL_SPLIT_CHAR) || sql.indexOf(SQL_SPLIT_CHAR) == endCol) {
            return Collections.singletonList(sql);
        }

        String[] lines = sql.split(SQL_NEW_LINE);
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row];
            for (int col = 0; col < line.toCharArray().length; col++) {
                char c = line.charAt(col);
                current += c;

                // case 1: check double quote: double quote is priority higher than single quote
                if ((c == '"' || c == '\'') && prev != '\\' && isComment == null) {
                    if (betweenQuotes != null && betweenQuotes == c) {
                        betweenQuotes = null;
                    } else if (betweenQuotes == null) {
                        betweenQuotes = c;
                    }
                }
                // case 2: check comment
                else if (c == '-' && prev == '-' && betweenQuotes == null && isComment == null) {
                    isComment = true;
                } else if (c == ';') {
                    if (betweenQuotes == null && isComment == null) {
                        current = current.trim();
                        current = current.substring(0, current.length() - 1);
                        if (current.length() > 1) {
                            statements.add(current.trim());
                        }
                        current = "";
                    }
                }
                if (prev == '\\' && betweenQuotes != null) {
                    c = '\0';
                }
                prev = c;
            }

            isComment = null;
            prev = SQL_NEW_LINE.charAt(0);
            if (!current.trim().equals("")) {
                current += SQL_NEW_LINE.charAt(0);
            }
        }

        if (current != null && current.trim().length() >= 1 && !current.equals(";")) {
            current = current.trim();
            statements.add(current);
        }
        return statements;
    }
}



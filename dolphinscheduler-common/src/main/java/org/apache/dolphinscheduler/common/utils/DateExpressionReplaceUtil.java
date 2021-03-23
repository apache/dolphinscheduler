/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DateExprReplaceUtil
 */
public class DateExpressionReplaceUtil {

    private DateExpressionReplaceUtil() {
        // Default Constructor
    }

    private static final Pattern EXPR_PATTERN = Pattern.compile("\\$\\{([\\s\\S]*?),([\\s\\S]*?),(\\s*-\\s*[0-9]+)?}");
    private static final Logger LOGGER = LoggerFactory.getLogger(DateExpressionReplaceUtil.class);

    /**
     * replace date expression
     * ${yyyy-MM-dd,MONTH,-1}=>${format,gain,offset}
     * @param source
     * @return
     */
    public static String replaceDateExpression(String source) {
        String str = source;
        Matcher m = EXPR_PATTERN.matcher(source);
        while (m.find()) {
            String replaceStr = m.group();
            if (StringUtils.isEmpty(m.group(1))
                    || StringUtils.isEmpty(m.group(2))
                    || StringUtils.isEmpty(m.group(3))) {
                return source;
            }

            String format = m.group(1);
            int gain = getGain(m.group(2));
            String offset = m.group(3);

            if (gain == -1) {
                return source;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(gain, Integer.parseInt(offset));
            date = c.getTime();
            str = str.replace(replaceStr, simpleDateFormat.format(date));
        }
        LOGGER.info("Succeed to convert [{}] into [{}]", source, str);
        return str;
    }

    private static int getGain(String gain) {
        switch (gain) {
            case Constants.YEAR:
                return Calendar.YEAR;
            case Constants.MONTH:
                return Calendar.MONTH;
            case Constants.DAY:
                return  Calendar.DAY_OF_MONTH;
            case Constants.HOUR:
                return Calendar.HOUR_OF_DAY;
            case Constants.MINUTE:
                return Calendar.MINUTE;
            case Constants.SECOND:
                return Calendar.SECOND;
            case Constants.WEEK:
                return Calendar.WEEK_OF_MONTH;
            default:
                return -1;
        }
    }

    public static void main(String[] args) {
        replaceDateExpression("${yyyy-MM-dd,DAY,-1}");
        replaceDateExpression("2132132131");
    }
}

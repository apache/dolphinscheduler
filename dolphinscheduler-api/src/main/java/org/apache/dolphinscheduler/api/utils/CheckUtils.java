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

package org.apache.dolphinscheduler.api.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.USER_PASSWORD_MAX_LENGTH;
import static org.apache.dolphinscheduler.common.constants.Constants.USER_PASSWORD_MIN_LENGTH;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

/**
 * check utils
 */
public class CheckUtils {

    private CheckUtils() {
        throw new IllegalStateException("CheckUtils class");
    }

    /**
     * check username
     *
     * @param userName user name
     * @return true if user name regex valid,otherwise return false
     */
    public static boolean checkUserName(String userName) {
        return regexChecks(userName, Constants.REGEX_USER_NAME);
    }

    /**
     * check email
     *
     * @param email email
     * @return true if email regex valid, otherwise return false
     */
    public static boolean checkEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        EmailValidator emailValidator = new EmailValidator();
        if (!emailValidator.isValid(email, null)) {
            return false;
        }
        // Email is at least a second-level domain name
        int indexDomain = email.lastIndexOf("@");
        String domainString = email.substring(indexDomain);
        return domainString.contains(".");
    }

    /**
     * check project description
     *
     * @param desc desc
     * @return true if description regex valid, otherwise return false
     */
    public static Map<String, Object> checkDesc(String desc) {
        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.isEmpty(desc) && desc.length() > 200) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG,
                    MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "desc length"));
        } else {
            result.put(Constants.STATUS, Status.SUCCESS);
        }
        return result;
    }

    /**
     * check extra info
     *
     * @param otherParams other parames
     * @return true if other parameters are valid, otherwise return false
     */
    public static boolean checkOtherParams(String otherParams) {
        return !StringUtils.isEmpty(otherParams) && !JSONUtils.checkJsonValid(otherParams);
    }

    /**
     * check password
     *
     * @param password password
     * @return true if password regex valid, otherwise return false
     */
    public static boolean checkPassword(String password) {
        return !StringUtils.isEmpty(password) && checkPasswordLength(password);
    }

    /**
     *  check password length
     * @param password password
     * @return true if password length valid, otherwise return false
     */
    public static boolean checkPasswordLength(String password) {
        return password.length() >= USER_PASSWORD_MIN_LENGTH && password.length() <= USER_PASSWORD_MAX_LENGTH;
    }

    /**
     * check phone phone can be empty.
     *
     * @param phone phone
     * @return true if phone regex valid, otherwise return false
     */
    public static boolean checkPhone(String phone) {
        return StringUtils.isEmpty(phone) || phone.length() == 11;
    }

    /**
     * check time zone parameter
     * @param timeZone timeZone
     * @return true if timeZone is valid, otherwise return false
     */
    public static boolean checkTimeZone(String timeZone) {
        try {
            ZoneId.of(timeZone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * regex check
     *
     * @param str     input string
     * @param pattern regex pattern
     * @return true if regex pattern is right, otherwise return false
     */
    private static boolean regexChecks(String str, Pattern pattern) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        return pattern.matcher(str).matches();
    }
}

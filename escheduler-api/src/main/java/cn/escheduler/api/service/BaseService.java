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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;

/**
 * base service
 */
public class BaseService {

    /**
     * check admin
     *
     * @param user
     * @return
     */
    protected boolean isAdmin(User user) {
        return user.getUserType() == UserType.ADMIN_USER;
    }

    /**
     * check admin
     *
     * @param loginUser
     * @param result
     * @return
     */
    protected boolean checkAdmin(User loginUser, Map<String, Object> result) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return true;
        }
        return false;
    }

    /**
     * put message to map
     *
     * @param result
     * @param status
     * @param statusParams
     */
    protected void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * put message to result object
     *
     * @param result
     * @param status
     */
    protected void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());

        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }

    }

    /**
     * get cookie info by name
     * @param request
     * @param name
     * @return get cookie info
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equalsIgnoreCase(name, cookie.getName())) {
                    return cookie;
                }
            }
        }

        return null;
    }

    /**
     * create tenant dir if not exists
     * @param tenantCode
     * @throws Exception
     */
    protected void createTenantDirIfNotExists(String tenantCode)throws Exception{

        String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
        String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
        /**
         * init resource path and udf path
         */
        HadoopUtils.getInstance().mkdir(resourcePath);
        HadoopUtils.getInstance().mkdir(udfsPath);
    }
}

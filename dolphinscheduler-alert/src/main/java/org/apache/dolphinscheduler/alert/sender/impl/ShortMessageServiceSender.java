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
package org.apache.dolphinscheduler.alert.sender.impl;

import org.apache.dolphinscheduler.alert.sender.Sender;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * SMS Sender
 */
public class ShortMessageServiceSender implements Sender {
    private static final Logger logger = LoggerFactory.getLogger(ShortMessageServiceSender.class);

    @Override
    public boolean send(Alert alert) {
        return false;
    }

    @Override
    public boolean sendGroup(Alert alert) {
        return false;
    }

    @Override
    public String getName() {
        return "SMS";
    }

    /**
     * get a list of SMS users
     * @param users
     * @return
     */
    private List<String> getReceiverListForSMS(List<User> users){
        List<String> list = new ArrayList<>();
        for (User user : users){
            list.add(user.getPhone());
        }
        return list;
    }
}

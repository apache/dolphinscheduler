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
package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.dao.entity.Alert;
import java.util.Optional;

/**
 * Alert Exception
 */
public class AlertException extends Exception {
    private final Optional<Alert> alert;

    public AlertException(String message) {
        super(message);
        this.alert = Optional.empty();
    }

    public AlertException(String message, Alert alert) {
        super(message);
        this.alert = Optional.of(alert);
    }

    public Optional<Alert> getAlert() {
        return alert;
    }

    @Override
    public String toString() {
        return "AlertException{" +
                "message=" + getMessage() +
                ", alert=" + alert +
                '}';
    }
}

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
package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * types for whether to send warning when process ending;
 */
public enum WarningType {
  /**
   * 0 do not send warning;
   * 1 send if process success;
   * 2 send if process failed;
   * 3 send if process ending;
   */
  NONE(0, "none"),
  SUCCESS(1, "success"),
  FAILURE(2, "failure"),
  ALL(3, "all");


  WarningType(int code, String descp){
    this.code = code;
    this.descp = descp;
  }

  @EnumValue
  private final int code;
  private final String descp;

  public int getCode() {
    return code;
  }

  public String getDescp() {
    return descp;
  }
}

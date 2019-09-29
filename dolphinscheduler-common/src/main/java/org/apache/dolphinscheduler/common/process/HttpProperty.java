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
package org.apache.dolphinscheduler.common.process;

import org.apache.dolphinscheduler.common.enums.HttpParametersType;

import java.util.Objects;

public class HttpProperty {
  /**
   * key
   */
  private String prop;

  /**
   *  httpParametersType
   */
  private HttpParametersType httpParametersType;

  /**
   * value
   */
  private String value;

  public HttpProperty() {
  }

  public HttpProperty(String prop, HttpParametersType httpParametersType, String value) {
    this.prop = prop;
    this.httpParametersType = httpParametersType;
    this.value = value;
  }

  /**
   * getter method
   *
   * @return the prop
   * @see HttpProperty#prop
   */
  public String getProp() {
    return prop;
  }

  /**
   * setter method
   *
   * @param prop the prop to set
   * @see HttpProperty#prop
   */
  public void setProp(String prop) {
    this.prop = prop;
  }

  /**
   * getter method
   *
   * @return the value
   * @see HttpProperty#value
   */
  public String getValue() {
    return value;
  }

  /**
   * setter method
   *
   * @param value the value to set
   * @see HttpProperty#value
   */
  public void setValue(String value) {
    this.value = value;
  }

  public HttpParametersType getHttpParametersType() {
    return httpParametersType;
  }

  public void setHttpParametersType(HttpParametersType httpParametersType) {
    this.httpParametersType = httpParametersType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    HttpProperty property = (HttpProperty) o;
    return Objects.equals(prop, property.prop) &&
            Objects.equals(value, property.value);
  }


  @Override
  public int hashCode() {
    return Objects.hash(prop, value);
  }

  @Override
  public String toString() {
    return "HttpProperty{" +
            "prop='" + prop + '\'' +
            ", httpParametersType=" + httpParametersType +
            ", value='" + value + '\'' +
            '}';
  }


}

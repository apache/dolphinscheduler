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

import org.apache.dolphinscheduler.common.enums.BasicType;
import org.apache.dolphinscheduler.common.enums.MeasureFailState;
import org.apache.dolphinscheduler.common.enums.MeasureOperation;

import java.math.BigDecimal;
import java.util.Objects;

public class MeasureProperty {

  /**
   * prop
   */
  private String prop;

  /**
   * basicType
   */
  private BasicType basicType;

  /**
   * operation
   */
  private MeasureOperation operation;

  /**
   * value
   */
  private String value;

  /**
   * matchRate
   */
  private BigDecimal matchRate;

  /**
   * isFail
   */
  private MeasureFailState failState;

  public MeasureProperty() {
  }

  public MeasureProperty(String prop, BasicType basicType, MeasureOperation operation, String value,BigDecimal matchRate, MeasureFailState failState) {
    this.prop = prop;
    this.basicType = basicType;
    this.operation = operation;
    this.value = value;
    this.matchRate = matchRate;
    this.failState = failState;
  }

  /**
   * getter method
   *
   * @return the prop
   * @see MeasureProperty#prop
   */
  public String getProp() {
    return prop;
  }

  /**
   * setter method
   *
   * @param prop the prop to set
   * @see MeasureProperty#prop
   */
  public void setProp(String prop) {
    this.prop = prop;
  }

  /**
   * getter method
   *
   * @return the value
   * @see MeasureProperty#value
   */
  public String getValue() {
    return value;
  }

  /**
   * setter method
   *
   * @param value the value to set
   * @see MeasureProperty#value
   */
  public void setValue(String value) {
    this.value = value;
  }

  public BasicType getBasicType() {
    return basicType;
  }

  public void setBasicType(BasicType basicType) {
    this.basicType = basicType;
  }

  public MeasureOperation getOperation() {
    return operation;
  }

  public void setOperation(MeasureOperation operation) {
    this.operation = operation;
  }

  public BigDecimal getMatchRate() {
    return matchRate;
  }

  public void setMatchRate(BigDecimal matchRate) {
    this.matchRate = matchRate;
  }

  public MeasureFailState getFailState() {
    return failState;
  }

  public void setFailState(MeasureFailState failState) {
    this.failState = failState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    MeasureProperty property = (MeasureProperty) o;
    return Objects.equals(prop, property.prop) &&
            Objects.equals(value, property.value);
  }


  @Override
  public int hashCode() {
    return Objects.hash(prop, value);
  }

  @Override
  public String toString() {
    return "MeasureProperty{" +
            "prop='" + prop + '\'' +
            ", basicType=" + basicType +
            ", operation=" + operation +
            ", value='" + value + '\'' +
            ", matchRate=" + matchRate +
            ", failState=" + failState +
            '}';
  }
}

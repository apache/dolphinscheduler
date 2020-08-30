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
/**
 * Find the container and initialize the chart according to the parameters, and then return one or a group of chart instances
 * @param {*} Target Chart component class
 * @param {*} el Selector or DOM object
 * @param {*} data data source
 * @param {*} options Optional
 */
export const init = (Target, el, data, options) => {
  const list = getChartContainers(el)
  const settings = Object.assign({}, { data }, options)
  const charts = list.map(element => {
    return new Target(element, settings)
  })
  return charts.length === 1 ? charts[0] : charts
}

/**
 * Unified chart container is an array of DOM elements
 * @param {*} el Selector or DOM object
 */
function getChartContainers (el) {
  // Parameter not transmitted, return directly
  if (!el) {
    return
  }
  if (typeof el === 'string') {
    if (el.startsWith('#')) {
      el = document.getElementById(el.slice(1))
    } else if (el.startsWith('.')) {
      el = document.getElementsByClassName(el.slice(1))
    } else {
      return
    }
  }
  if (!el) {
    throw new Error('No corresponding DOM object found!')
  }
  let list
  if (Object.prototype.isPrototypeOf.call(HTMLElement.prototype, el)) {
    list = new Array(el)
  } else {
    list = Array.from(el)
  }
  if (!list) {
    throw new Error('No corresponding DOM object found!')
  }
  return list
}

/**
 * Detects whether the specified property name exists in the specified object
 * @param {Object} model Model to be tested
 * @param  {...any} params Property name to be tested
 */
export const checkKeyInModel = (model, ...params) => {
  for (const key of params) {
    if (!Object.prototype.hasOwnProperty.call(model, key)) {
      throw new Error('Data format error! The specified property was not found:' + key)
    }
  }
}

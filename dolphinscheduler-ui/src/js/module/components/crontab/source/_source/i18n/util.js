
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
 * template
 *
 * @param {String} string
 * @param {Array} ...args
 * @return {String}
 */
const { hasOwnProperty } = {}
const RE_NARGS = /(%|)\{([0-9a-zA-Z_]+)\}/g
const hasOwn = (o, k) => hasOwnProperty.call(o, k)
const template = (string, ...args) => {
  if (args.length === 1 && typeof args[0] === 'object') {
    args = args[0]
  }
  if (!args || !args.hasOwnProperty) {
    args = {}
  }
  return string.replace(RE_NARGS, (match, prefix, i, index) => {
    let result
    if (string[index - 1] === '{' &&
      string[index + match.length] === '}') {
      return i
    } else {
      result = hasOwn(args, i) ? args[i] : null
      if (result === null || result === undefined) {
        return ''
      }
      return result
    }
  })
}

export { template }

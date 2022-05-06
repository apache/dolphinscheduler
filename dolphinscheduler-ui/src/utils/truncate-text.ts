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
 * truncateText('ALongText', 4) => 'ALon...'
 * @param {number} limit
 * @param {string} text
 * Each Chinese character is equal to two chars
 */
const truncateText = (text: string, n: number) => {
  const exp = /[\u4E00-\u9FA5]/
  let res = ''
  let len = text.length
  const chinese = text.match(new RegExp(exp, 'g'))
  if (chinese) {
    len += chinese.length
  }
  if (len > n) {
    let i = 0
    let acc = 0
    while (true) {
      const char = text[i]
      if (exp.test(char)) {
        acc += 2
      } else {
        acc++
      }
      if (acc > n) break
      res += char
      i++
    }
    res += '...'
  } else {
    res = text
  }
  return res
}

export default truncateText

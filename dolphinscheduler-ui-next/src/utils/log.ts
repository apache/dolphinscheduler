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

const log = {
  capsule: (title: string, text: string, type?: string) => {},
  error: (info: any) => {}
}

/**
 * @description Returns the color value of the style
 * @param {String} type The style name [ primary | success | warning | error ]
 */
const typeColor = (type = 'primary') => {
  let color = ''
  switch (type) {
    case 'primary':
      color = '#1890ff'
      break
    case 'success':
      color = '#52c41a'
      break
    case 'warning':
      color = '#faad14'
      break
    case 'error':
      color = '#ff4d4f'
      break
    default:
      break
  }
  return color
}

/**
 * @description capsule
 * @param {String} title title text
 * @param {String} text info text
 * @param {String} type style
 */
log.capsule = (title: string, text: string, type: string = 'primary') => {
  console.log(
    `%c ${title} %c ${text} %c`,
    'background:#35495E; padding: 2px ; border-radius: 3px 0 0 3px; color: #fff;',
    `background:${typeColor(
      type
    )}; padding: 2px; border-radius: 0 3px 3px 0;  color: #fff;`,
    'background:transparent'
  )
}

/**
 * @description Prints text in error style
 */
log.error = function (info) {
  console.group('error info')
  console.log('responseURL: ', `${info.config.baseURL}${info.config.url}`)
  console.log('msg: ', info.data.msg)
  console.groupEnd()
}

export default log

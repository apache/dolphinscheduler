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

import i18n from '@/module/i18n'
/**
 * download file
 */
const downloadFile = ($url, $obj) => {
  const param = {
    url: $url,
    obj: $obj
  }

  if (!param.url) {
    this.$message.warning(`${i18n.$t('Unable to download without proper url')}`)
    return
  }

  const generatorInput = function (obj) {
    let result = ''
    const keyArr = Object.keys(obj)
    keyArr.forEach(function (key) {
      result += "<input type='hidden' name = '" + key + "' value='" + obj[key] + "'>"
    })
    return result
  }
  $(`<form action="${param.url}" method="get">${generatorInput(param.obj)}</form>`).appendTo('body').submit().remove()
}

export { downloadFile }

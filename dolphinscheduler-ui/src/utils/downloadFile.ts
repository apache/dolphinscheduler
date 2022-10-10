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

const apiPrefix = '/dolphinscheduler'
const reSlashPrefix = /^\/+/

const resolveURL = (url: string) => {
  if (url.indexOf('http') === 0) {
    return url
  }
  if (url.charAt(0) !== '/') {
    return `${apiPrefix}/${url.replace(reSlashPrefix, '')}`
  }

  return url
}

const downloadFile = (url: string, obj?: any) => {
  const param: any = {
    url: resolveURL(url),
    obj: obj || {}
  }

  const form = document.createElement('form')
  form.action = param.url
  form.method = 'get'
  form.style.display = 'none'
  Object.keys(param.obj).forEach((key) => {
    const input = document.createElement('input')
    input.type = 'hidden'
    input.name = key
    input.value = param.obj[key]
    form.appendChild(input)
  })
  const button = document.createElement('input')
  button.type = 'submit'
  form.appendChild(button)
  document.body.appendChild(form)
  form.submit()
  document.body.removeChild(form)
}

export default downloadFile

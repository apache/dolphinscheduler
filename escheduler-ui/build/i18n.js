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

const fs = require('fs');
const path = require('path')
const glob = require('globby')

function moduleName (modules) {
  let filename = path.basename(modules)
  let parts = filename.split('.')
  parts.pop()
  filename = parts.join('.')
  return path.dirname(modules) + '/' + filename
}

const jsEntry = () => {
  const obj = {}
  const files = glob.sync([
    './src/js/conf/login/**/*.vue',
    './src/js/conf/login/**/*.js',
    './src/js/conf/home/**/**/**/**/**/**/**/**/*.vue',
    './src/js/conf/home/**/**/**/**/**/**/**/**/*.js',
    './src/js/module/**/**/**/**/**/*.vue',
    './src/js/module/**/**/**/**/**/*.js'
  ])
  files.forEach(val => {
    let parts = val.split(/[\\/]/)
    parts.shift()
    parts.shift()
    let modules = parts.join('/')
    let entry = moduleName(modules)
    obj[entry] = val
  })
  return obj
}
/* eslint-disable */
let reg = /\$t\([\w,""''“”~\-\s.?!，。：；《》、\+\/<>()？！\u4e00-\u9fa5]*\)/g
let map = {}
let entryPathList = ''
let matchPathList = ''
let jsEntryObj = jsEntry()

for (let i in jsEntryObj) {
  entryPathList += jsEntryObj[i] + '\n'
  let data = fs.readFileSync(path.join(jsEntryObj[i]), 'utf-8')
  if (reg.test(data)) {
    matchPathList += jsEntryObj[i] + '\n'
    let str = data.replace(/[""'']/g, '')
    str.replace(reg, function () {
      if (arguments && arguments[0]) {
        let key = arguments[0]
        key = key.substring(3, key.length - 1)
        map[key] = key
      }
    })
  }
}

let outPath = path.join(__dirname, '../src/js/module/i18n/locale/zh_CN.js')
fs.unlink(outPath, (err) => {
  if (err) {
    console.error('删除zh_CN.js文件出错 -- \n', err)
  } else {
    console.log('删除zh_CN.js文件成功')
  }
})
fs.writeFile(outPath, 'export default ' + JSON.stringify(map, null, 2), function (err) {
  if (err) {
    console.error('写入zh_CN.js文件出错 -- \n', err)
  } else {
    console.log('写入zh_CN.js文件成功')
  }
})

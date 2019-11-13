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

var fs = require('fs')
var path = require('path')
var request = require('request')
var cdnUrl = 'https://s1.analysys.cn/libs/??'
var version = '1.0.0'

// js集合
var jslibs = {
  'es5': [
    'es5-shim/4.5.7/es5-shim.min.js',
    'es5-shim/4.5.7/es5-sham.min.js'
  ],
  '3rd': [
    'vue/2.5.2/vue.js',
    'vue-router/2.7.0/vue-router.min.js',
    'vuex/3.0.0/vuex.min.js',
    'jquery/3.3.1/jquery.min.js',
    'lodash.js/4.17.5/lodash.min.js',
    'jqueryui/1.12.1/jquery-ui.min.js',
    'twitter-bootstrap/3.3.7/js/bootstrap.min.js',
    'jsPlumb/2.8.5/js/jsplumb.min.js',
    'clipboard.js/2.0.1/clipboard.min.js',
    'd3/3.3.6/d3.min.js',
    'echarts/4.1.0/echarts.min.js',
    'dayjs/1.7.8/dayjs.min.js',
    'codemirror/5.43.0/codemirror.min.js',
    'codemirror/5.43.0/mode/sql/sql.min.js',
    'codemirror/5.43.0/addon/hint/show-hint.min.js',
    'codemirror/5.43.0/addon/hint/sql-hint.min.js',
    'codemirror/5.43.0/mode/textile/textile.min.js',
    'codemirror/5.43.0/mode/shell/shell.min.js',
    'codemirror/5.43.0/mode/python/python.min.js',
    'codemirror/5.43.0/addon/hint/xml-hint.min.js',
    'codemirror/5.43.0/mode/xml/xml.min.js',
    'html2canvas/0.5.0-beta4/html2canvas.min.js',
    'canvg/1.5/canvg.min.js'
  ],
  'local': []
}

// css
csslibs = {
  'base': [
    'normalize/7.0.0/normalize.min.css',
    'twitter-bootstrap/3.3.7/css/bootstrap.min.css',
    '-/@analysys/reset.css@1.0.1',
    '-/@vue/animate.css@'
  ],
  '3rd': [
    'highlight.js/9.13.1/styles/vs.min.css',
    'jsPlumb/2.8.5/css/jsplumbtoolkit-defaults.min.css',
    'codemirror/5.43.0/codemirror.min.css',
    'codemirror/5.20.0/theme/mdn-like.min.css',
    'codemirror/5.43.0/addon/hint/show-hint.min.css'
  ]
}

// Create folder directory
var dirPath = path.resolve(__dirname, '..', 'src/combo/' + version)

if (!fs.existsSync(dirPath)) {
  fs.mkdirSync(dirPath)
  console.log('Folder created successfully')
} else {
  console.log('Folder already exists')
}

var jsKeys = Object.keys(jslibs)
var jsUrl = jsKeys.map(v => {
  return jslibs[v].join()
})

jsUrl.forEach((v, i) => {
  var url = cdnUrl + v
  console.log(url)
  var stream = fs.createWriteStream(path.join(dirPath, jsKeys[i] + '.js'), { encoding: 'utf-8' })
  request(url).pipe(stream).on('close', function (err) {
    if (!err) {
      console.log('file[' + version + '/' + jsKeys[i] + '.js' + ']Download completed')
    }
  })
})

var cssKeys = Object.keys(csslibs)
var cssUrl = cssKeys.map(v => {
  return csslibs[v].join()
})

cssUrl.forEach((v, i) => {
  var url = cdnUrl + v
  console.log(url)
  var stream = fs.createWriteStream(path.join(dirPath, cssKeys[i] + '.css'), { encoding: 'utf-8' })
  request(url).pipe(stream).on('close', function (err) {
    if (!err) {
      console.log('file[' + version + '/' + cssKeys[i] + '.css' + ']Download completed')
    }
  })
})

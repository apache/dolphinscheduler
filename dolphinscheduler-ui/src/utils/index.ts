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

import mapping from './mapping'
import regex from './regex'
import truncateText from './truncate-text'
import log from './log'
import downloadFile from './downloadFile'
import copy from './clipboard'
import removeUselessChildren from './tree-format'
import isJson from './json'

const utils = {
  mapping,
  regex,
  truncateText,
  log,
  downloadFile,
  copy,
  removeUselessChildren,
  isJson
}

export default utils

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

const assert = require('node:assert/strict')
const { existsSync, readFileSync } = require('node:fs')
const { join } = require('node:path')
const test = require('node:test')
const ts = require('typescript')

const filename = join(
  __dirname,
  '../src/views/projects/task/components/node/modal-style.ts'
)
const modalStyle = { exports: {} }
if (existsSync(filename)) {
  const source = ts.transpileModule(readFileSync(filename, 'utf8'), {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020
    }
  }).outputText
  new Function('module', 'exports', 'require', source)(
    modalStyle,
    modalStyle.exports,
    require
  )
}

test('widens SQL and Shell task dialogs through the modal style', () => {
  const styleFor = modalStyle.exports.nodeDetailModalStyle
  assert.equal(
    typeof styleFor,
    'function',
    'node detail modal style must be implemented'
  )

  assert.deepEqual(styleFor('SQL'), { width: 'min(1400px, 90vw)' })
  assert.deepEqual(styleFor('SHELL'), { width: 'min(1400px, 90vw)' })
  assert.equal(styleFor('PYTHON'), undefined)
})

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { join } = require('node:path')
const test = require('node:test')
const ts = require('typescript')

const filename = join(
  __dirname,
  '../src/views/projects/workflow/instance/gantt/identity.ts'
)
const identity = { exports: {} }
const source = ts.transpileModule(readFileSync(filename, 'utf8'), {
  compilerOptions: {
    module: ts.ModuleKind.CommonJS,
    target: ts.ScriptTarget.ES2020
  }
}).outputText
new Function('module', 'exports', 'require', source)(
  identity,
  identity.exports,
  require
)

test('leaving the gantt route invalidates its request identity', () => {
  const valid = identity.exports.isValidGanttIdentity

  assert.equal(valid([396396, 11364899434912]), true)
  assert.equal(valid([Number.NaN, 11364899434912]), false)
  assert.equal(valid([396396, Number.NaN]), false)
  assert.equal(valid([0, 11364899434912]), false)
})

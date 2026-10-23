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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { join } = require('node:path')
const test = require('node:test')
const ts = require('typescript')

const filename = join(__dirname, '../src/service/modules/log/load-pages.ts')
const source = ts.transpileModule(readFileSync(filename, 'utf8'), {
  compilerOptions: {
    module: ts.ModuleKind.CommonJS,
    target: ts.ScriptTarget.ES2020
  }
}).outputText

function loader(queryLog) {
  const module = { exports: {} }
  new Function('module', 'exports', 'require', source)(
    module,
    module.exports,
    () => ({ queryLog })
  )
  return module.exports.loadLogPages
}

test('paginates logs using the returned line count', async () => {
  const requests = []
  const messages = []
  const load = loader(async ({ skipLineNum }) => {
    requests.push(skipLineNum)
    return skipLineNum === 0
      ? { message: 'first', lineNum: 3 }
      : { message: '', lineNum: 0 }
  })
  const result = await load(42, (message) => messages.push(message))
  assert.deepEqual(requests, [0, 3])
  assert.deepEqual(messages, ['first'])
  assert.deepEqual(result, { cancelled: false, limited: false })
})

test('stops at the preview line limit', async () => {
  const requests = []
  const load = loader(async ({ skipLineNum }) => {
    requests.push(skipLineNum)
    return { message: 'page', lineNum: 1000 }
  })
  const result = await load(42, () => {}, { maxLines: 2000 })
  assert.deepEqual(requests, [0, 1000])
  assert.deepEqual(result, { cancelled: false, limited: true })
})

test('discards a page after the selected log changes', async () => {
  let current = true
  const messages = []
  const load = loader(async () => {
    current = false
    return { message: 'stale', lineNum: 1 }
  })
  const result = await load(42, (message) => messages.push(message), {
    isCurrent: () => current
  })
  assert.deepEqual(messages, [])
  assert.deepEqual(result, { cancelled: true, limited: false })
})

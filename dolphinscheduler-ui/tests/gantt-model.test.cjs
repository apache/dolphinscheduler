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
const { test } = require('node:test')
const fs = require('node:fs')
const path = require('node:path')
const ts = require('typescript')
const filename = path.resolve(
  __dirname,
  '../src/views/projects/workflow/instance/gantt/model.ts'
)
const model = { exports: {} }
if (fs.existsSync(filename)) {
  const source = ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020
    }
  }).outputText
  new Function('module', 'exports', 'require', source)(
    model,
    model.exports,
    require
  )
}
const build = (...args) => {
  assert.equal(
    typeof model.exports.buildGanttModel,
    'function',
    'gantt data normalization must be implemented'
  )
  return model.exports.buildGanttModel(...args)
}
const workflow = {
  name: 'pipeline',
  state: 'SUCCESS',
  startTime: 1000,
  endTime: 11000,
  dagData: {
    taskDefinitionList: [
      { code: 1, name: 'extract' },
      { code: 2, name: 'load' },
      { code: 3, name: 'pending' }
    ]
  }
}

test('counts definition nodes including unsubmitted tasks and deduplicates retry attempts', () => {
  const result = build(
    workflow,
    [
      {
        id: 1,
        taskCode: 1,
        name: 'extract',
        state: 'FAILURE',
        flag: 'NO',
        startTime: 1000,
        endTime: 2000
      },
      {
        id: 3,
        taskCode: 1,
        name: 'extract',
        state: 'SUCCESS',
        startTime: 2000,
        endTime: 5000
      },
      {
        id: 4,
        taskCode: 2,
        name: 'load',
        state: 'RUNNING_EXECUTION',
        startTime: 5000
      }
    ],
    undefined,
    10000
  )
  assert.equal(result.stats.total, 3)
  assert.equal(result.stats.submitted, 2)
  assert.equal(result.stats.pending, 1)
  assert.equal(result.stats.success, 1)
  assert.equal(result.stats.failed, 0)
  assert.equal(result.rows[0].id, 3)
  assert.equal(result.rows[2].start, null)
})

test('uses millisecond gantt timestamps and workflow elapsed duration for percentages', () => {
  const result = build(
    workflow,
    [
      {
        id: 1,
        taskCode: 1,
        name: 'extract',
        state: 'SUCCESS',
        startTime: 1000,
        endTime: 1000
      }
    ],
    {
      tasks: [{ taskName: 'extract', startDate: [1100], endDate: [1287] }],
      taskNames: [1, 2, 3]
    },
    20000
  )
  assert.equal(result.rows[0].duration, 187)
  assert.equal(result.rows[0].percent, 1.87)
  assert.equal(result.duration, 10000)
})

test('indexes gantt tasks once when building large workflows', () => {
  const taskCount = 100
  const definitions = Array.from({ length: taskCount }, (_, index) => ({
    code: index + 1,
    name: `task-${index + 1}`
  }))
  const instances = definitions.map(({ code, name }) => ({
    id: code,
    taskCode: code,
    name,
    state: 'SUCCESS',
    startTime: code * 1000,
    endTime: code * 1000 + 500
  }))
  let taskReads = 0
  const ganttTasks = new Proxy(
    definitions.map(({ code, name }) => ({
      taskName: name,
      startDate: [code * 1000],
      endDate: [code * 1000 + 500],
      ...(code === 1 ? { isoStart: '1970-01-01T00:00:01Z' } : {})
    })),
    {
      get(target, property, receiver) {
        if (typeof property === 'string' && /^\d+$/.test(property)) taskReads++
        return Reflect.get(target, property, receiver)
      }
    }
  )

  build(
    {
      state: 'SUCCESS',
      startTime: 1000,
      endTime: taskCount * 1000 + 500,
      dagData: { taskDefinitionList: definitions }
    },
    instances,
    { taskNames: definitions.map(({ code }) => code), tasks: ganttTasks }
  )

  assert.ok(
    taskReads <= taskCount * 3,
    `expected a linear scan, read ${taskReads} entries for ${taskCount} tasks`
  )
})

test('extends running bars and workflow bounds with current time but never stretches completed bars', () => {
  const running = { ...workflow, state: 'RUNNING_EXECUTION', endTime: null }
  const tasks = [
    {
      id: 1,
      taskCode: 1,
      name: 'extract',
      state: 'SUCCESS',
      startTime: 1000,
      endTime: 3000
    },
    {
      id: 2,
      taskCode: 2,
      name: 'load',
      state: 'RUNNING_EXECUTION',
      startTime: 3000
    }
  ]
  const before = build(running, tasks, undefined, 6000)
  const after = build(running, tasks, undefined, 11000)
  assert.equal(before.duration, 5000)
  assert.equal(after.duration, 10000)
  assert.equal(after.rows[0].duration, 2000)
  assert.equal(after.rows[1].duration, 8000)
  assert.equal(after.stats.running, 1)
})

test('missing dates, empty workflows, and zero-duration tasks produce finite ranges', () => {
  const empty = build({ state: 'SUBMITTED_SUCCESS' }, [], undefined, 1000)
  assert.equal(empty.stats.total, 0)
  assert.ok(empty.axisDuration > 0)
  const result = build(
    workflow,
    [{ id: 1, taskCode: 1, name: 'extract', state: 'SUBMITTED_SUCCESS' }],
    undefined,
    12000
  )
  assert.equal(result.rows[0].start, null)
  assert.equal(result.rows[0].duration, null)
  assert.equal(result.stats.waiting, 1)
})

test('parallel task percentages are wall-clock shares, not normalized to sum to 100', () => {
  const result = build(
    workflow,
    [1, 2].map((code) => ({
      id: code,
      taskCode: code,
      name: String(code),
      state: 'SUCCESS',
      startTime: 1000,
      endTime: 11000
    })),
    undefined,
    12000
  )
  assert.equal(result.rows[0].percent, 100)
  assert.equal(result.rows[1].percent, 100)
})

test('queued workflows use the server UTC clock even before any task has started', () => {
  const result = build(
    { state: 'RUNNING_EXECUTION', startTime: '2026-09-16 08:00:00' },
    [],
    undefined,
    Date.parse('2026-09-16T08:00:05Z')
  )
  assert.equal(result.duration, 5000)
})

test('a subsecond workflow fills the percentage axis without an artificial one-second floor', () => {
  const result = build({ state: 'SUCCESS', startTime: 1000, endTime: 1500 }, [
    {
      id: 1,
      taskCode: 1,
      name: 'short',
      state: 'SUCCESS',
      startTime: 1000,
      endTime: 1500
    }
  ])
  assert.equal(result.axisDuration, 500)
  assert.equal(result.rows[0].percent, 100)
})

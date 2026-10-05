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

// Run from dolphinscheduler-ui: node tests/task-execution-type.cjs [commit-SHA]
// Uses the installed UI dependencies; no additional test framework is required.
// Executes the production callback, models, defaults, and serializers. Unrelated
// field rendering and store services are stubbed. This is not a browser/API E2E.
const assert = require('assert/strict')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const { execFileSync } = require('child_process')
const ts = require('typescript')
const vue = require('vue')
const ui = path.resolve(__dirname, '..')
const revision = process.argv[2]
if (revision && !/^[a-f0-9]{40}$/.test(revision)) {
  throw new Error('Provide an exact 40-character commit SHA')
}
const taskRoot = 'views/projects/task/components/node/'
const source = (file) =>
  revision
    ? execFileSync(
        'git',
        ['show', `${revision}:dolphinscheduler-ui/src/${file}`],
        {
          cwd: path.dirname(ui),
          encoding: 'utf8'
        }
      )
    : fs.readFileSync(path.join(ui, 'src', file), 'utf8')
const compilerOptions = {
  module: ts.ModuleKind.CommonJS,
  target: ts.ScriptTarget.ES2020,
  esModuleInterop: true
}
const cache = new Map()
const fields = new Proxy(
  { __esModule: true },
  { get: (target, key) => (key === '__esModule' ? true : () => []) }
)
const store = {
  updateDefinition() {},
  getName: 'execution-mode-test',
  getPreTasks: []
}
function load(file) {
  if (cache.has(file)) return cache.get(file)
  const module = { exports: {} }
  const code = ts.transpileModule(source(file), { compilerOptions }).outputText
  function localRequire(id) {
    if (id === './dag-hooks') {
      return {
        useCellUpdate: () => ({
          addNode() {},
          removeNode() {},
          getSources: () => [],
          getTargets: () => [],
          setNodeName() {},
          setNodeFillColor() {},
          setNodeExecuteType() {},
          setNodeEdge() {}
        })
      }
    }
    if (id === '../fields/index') return fields
    if (id === './tasks') {
      return {
        __esModule: true,
        default: {
          SHELL: load(taskRoot + 'tasks/use-shell.ts').useShell,
          FLINK: load(taskRoot + 'tasks/use-flink.ts').useFlink,
          FLINK_STREAM: load(taskRoot + 'tasks/use-flink-stream.ts')
            .useFlinkStream,
          SEATUNNEL: load(taskRoot + 'tasks/use-sea-tunnel.ts').useSeaTunnel
        }
      }
    }
    if (id === '@/components/form/get-elements-by-json') {
      return { __esModule: true, default: () => ({ rules: {}, elements: [] }) }
    }
    if (id === '@/store/project/task-node') {
      return { useTaskNodeStore: () => store }
    }
    if (!id.startsWith('.') && !id.startsWith('@/')) return require(id)
    const target = id.startsWith('@/')
      ? id.slice(2)
      : path.posix.join(path.posix.dirname(file), id)
    return load(target + '.ts')
  }
  vm.runInThisContext(`(function(require,module,exports){${code}\n})`, {
    filename: file
  })(localRequire, module, module.exports)
  cache.set(file, module.exports)
  return module.exports
}

// Extract the actual TSX callback so the test cannot diverge from its guard.
const modal = ts.createSourceFile(
  'detail-modal.tsx',
  source(taskRoot + 'detail-modal.tsx'),
  ts.ScriptTarget.Latest,
  true,
  ts.ScriptKind.TSX
)
let callback
function visit(node) {
  if (
    ts.isVariableDeclaration(node) &&
    node.name.getText(modal) === 'onTaskTypeChange'
  ) {
    callback = node.initializer.getText(modal)
  }
  ts.forEachChild(node, visit)
}
visit(modal)
assert(callback, 'onTaskTypeChange callback must exist')
const changeCode = ts.transpileModule(`(${callback})(nextType)`, {
  compilerOptions
}).outputText
function changeTaskType(data, nextType) {
  vm.runInNewContext(changeCode, {
    props: { data },
    nextType,
    initHeaderLinks() {}
  })
}
const { useTask } = load(taskRoot + 'use-task.ts')
const { formatParams, formatModel } = load(taskRoot + 'format-data.ts')
const { useForm } = load('components/form/use-form.ts')
const makeForm = (data, from = 0) =>
  useTask({ data, projectCode: 1, from }).model
const save = (model) => formatParams(model).taskDefinitionJsonObj
function reopen(data) {
  const model = makeForm(data)
  const form = useForm()
  form.state.formRef = { model }
  form.setValues(formatModel(data))
  return form.getValues()
}
const checks = []
function check(name, run) {
  try {
    run()
    checks.push({ name, result: 'PASS' })
  } catch (error) {
    checks.push({ name, result: 'FAIL', error: error.message })
  }
}

// Mount the real editor hook so its lifecycle and reactive ownership are active.
// Graph rendering is irrelevant to the task-definition Cancel/Save contract.
function makeEditor() {
  const definition = vue.ref({
    workflowDefinition: {},
    workflowTaskRelationList: [],
    taskDefinitionList: [
      {
        id: 5,
        code: 20,
        version: 3,
        name: 'saved',
        taskType: 'SEATUNNEL',
        taskExecuteType: 'STREAM',
        taskParams: { localParams: [{ prop: 'key', value: 'original' }] }
      }
    ]
  })
  let editor
  const app = vue
    .createRenderer({
      createComment: () => ({}),
      insert() {},
      remove() {},
      parentNode() {},
      nextSibling() {}
    })
    .createApp({
      setup() {
        editor = load(
          'views/projects/workflow/components/dag/use-task-edit.ts'
        ).useTaskEdit({ graph: vue.ref(), definition })
        return () => null
      }
    })
  app.mount({})
  return { definition, editor, close: () => app.unmount() }
}

for (const nextType of ['SHELL', 'FLINK_STREAM', 'SEATUNNEL']) {
  check(
    `Cancel ${nextType} edit preserves saved task and nested parameters`,
    () => {
      const { definition, editor, close } = makeEditor()
      try {
        const original = definition.value.taskDefinitionList[0]
        const snapshot = JSON.parse(JSON.stringify(original))
        editor.editTask(20)
        changeTaskType(editor.currTask.value, nextType)
        editor.currTask.value.taskParams.localParams[0].value = 'draft'
        editor.taskCancel()
        assert.equal(editor.taskModalVisible.value, false)
        assert.equal(definition.value.taskDefinitionList[0], original)
        assert.deepEqual(JSON.parse(JSON.stringify(original)), snapshot)
      } finally {
        close()
      }
    }
  )
}

for (const nextType of ['SEATUNNEL', 'FLINK_STREAM', 'SHELL']) {
  check(
    `Save ${nextType} edit commits mode and preserves task identity`,
    () => {
      const { definition, editor, close } = makeEditor()
      try {
        const original = definition.value.taskDefinitionList[0]
        const parent = definition.value
        editor.editTask(20)
        changeTaskType(editor.currTask.value, nextType)
        const model = makeForm(editor.currTask.value, 1)
        model.taskExecuteType = nextType === 'FLINK_STREAM' ? 'STREAM' : 'BATCH'
        model.name = 'edited'
        editor.taskConfirm({ data: model })
        const saved = definition.value.taskDefinitionList[0]
        assert.equal(editor.workflowDefinition.value, parent)
        assert.notEqual(saved, original)
        assert.equal(saved.taskType, nextType)
        assert.equal(saved.taskExecuteType, model.taskExecuteType)
        assert.equal(saved.name, 'edited')
        assert.equal(saved.id, 5)
        assert.equal(saved.code, 20)
        assert.equal(saved.version, 3)
        assert.equal(editor.taskModalVisible.value, false)
      } finally {
        close()
      }
    }
  )
}

for (const [oldType, oldMode, nextType, expected] of [
  ['SHELL', 'BATCH', 'FLINK_STREAM', 'STREAM'],
  ['FLINK', 'BATCH', 'FLINK_STREAM', 'STREAM'],
  ['FLINK_STREAM', 'STREAM', 'SHELL', 'BATCH'],
  ['SEATUNNEL', 'STREAM', 'SHELL', 'BATCH'],
  ['FLINK_STREAM', 'STREAM', 'SEATUNNEL', 'BATCH'],
  ['SHELL', 'BATCH', 'SEATUNNEL', 'BATCH']
]) {
  check(`${oldType}/${oldMode} -> ${nextType}/${expected}`, () => {
    // from=1 exposes the component's dormant task-type selector.
    const data = vue.reactive({
      id: '5',
      code: 20,
      name: 'saved',
      taskType: oldType,
      taskExecuteType: oldMode,
      taskParams: {}
    })
    const params = data.taskParams
    changeTaskType(data, nextType)
    const payload = save(makeForm(data, 1))
    assert.equal(payload.taskType, nextType)
    assert.equal(payload.taskExecuteType, expected)
    assert.equal(data.code, 20)
    assert.equal(data.name, 'saved')
    assert.equal(data.taskParams, params)
  })
}
for (const mode of ['BATCH', 'STREAM']) {
  check(`same-type SeaTunnel keeps ${mode}`, () => {
    const data = { taskType: 'SEATUNNEL', taskExecuteType: mode }
    changeTaskType(data, 'SEATUNNEL')
    assert.deepEqual(data, { taskType: 'SEATUNNEL', taskExecuteType: mode })
    assert.equal(save(makeForm(data, 1)).taskExecuteType, mode)
  })
}
check('loaded SeaTunnel STREAM initializes without losing its mode', () => {
  assert.equal(
    makeForm({ id: '5', taskType: 'SEATUNNEL', taskExecuteType: 'STREAM' })
      .taskExecuteType,
    'STREAM'
  )
})
for (const useCustom of [true, false]) {
  check(`SeaTunnel save/reopen with useCustom=${useCustom}`, () => {
    let model = makeForm({ taskType: 'SEATUNNEL' })
    model.useCustom = useCustom
    model.rawScript = 'env { job.mode = "STREAMING" }'
    model.resourceList = useCustom ? [] : ['/seatunnel/config.conf']
    for (const mode of ['BATCH', 'STREAM', 'BATCH']) {
      model.taskExecuteType = mode
      const payload = save(model)
      assert.equal(payload.taskExecuteType, mode)
      assert.equal('taskExecuteType' in payload.taskParams, false)
      model = reopen(JSON.parse(JSON.stringify(payload)))
      assert.equal(model.taskExecuteType, mode)
      assert.equal(model.useCustom, useCustom)
      assert.equal(
        model.rawScript,
        useCustom ? 'env { job.mode = "STREAMING" }' : ''
      )
      assert.deepEqual(
        Array.from(model.resourceList),
        useCustom ? [] : ['/seatunnel/config.conf']
      )
    }
  })
}
for (const [taskType, mode] of [
  ['FLINK_STREAM', 'STREAM'],
  ['SEATUNNEL', 'BATCH']
]) {
  check(`legacy ${taskType} definition defaults to ${mode}`, () => {
    const model = reopen({ id: '5', taskType, taskParams: {} })
    assert.equal(model.taskExecuteType, mode)
    assert.equal(save(model).taskExecuteType, mode)
  })
}
for (const item of checks) {
  process.stdout.write(
    `${item.result} ${item.name}${item.error ? ': ' + item.error : ''}\n`
  )
}
if (process.env.TASK_EXECUTION_REPORT_PATH) {
  fs.writeFileSync(
    process.env.TASK_EXECUTION_REPORT_PATH,
    JSON.stringify({ revision: revision || 'working-tree', checks }, null, 2) +
      '\n'
  )
}
process.exitCode = checks.some((item) => item.result === 'FAIL') ? 1 : 0

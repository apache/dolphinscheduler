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

// Run from dolphinscheduler-ui: node tests/task-execution-graph.cjs [commit-SHA]
// Executes production editor, graph update/builder, serializer, business mapper,
// and stream-table renderer/service code. X6 storage and HTTP are adapters;
// this does not render a browser, validate forms, or execute task engines.
const assert = require('assert/strict')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const { execFileSync } = require('child_process')
const ts = require('typescript')
const vue = require('vue')
const { cloneDeep, get, set } = require('lodash')
const { NButton } = require('naive-ui')
const ui = path.resolve(__dirname, '..')
const revision = process.argv[2]
if (revision && !/^[a-f0-9]{40}$/.test(revision)) {
  throw new Error('Provide an exact 40-character commit SHA')
}
const dagRoot = 'views/projects/workflow/components/dag/'
const cache = new Map()
const requests = []
const messages = []
const axios = (request) => {
  requests.push(cloneDeep(request))
  return Promise.resolve({ total: 0, totalList: [], totalPage: 0 })
}
const source = (file) =>
  revision
    ? execFileSync(
        'git',
        ['show', `${revision}:dolphinscheduler-ui/src/${file}`],
        { cwd: path.dirname(ui), encoding: 'utf8' }
      )
    : fs.readFileSync(path.join(ui, 'src', file), 'utf8')

function load(file) {
  if (cache.has(file)) return cache.get(file)
  const module = { exports: {} }
  // Vite's asset URL is irrelevant to graph data and unavailable in CommonJS.
  const code = ts.transpileModule(
    source(file).replace(/import\.meta\.env\.BASE_URL/g, JSON.stringify('/')),
    {
      compilerOptions: {
        module: ts.ModuleKind.CommonJS,
        target: ts.ScriptTarget.ES2020,
        esModuleInterop: true
      }
    }
  ).outputText
  function localRequire(id) {
    if (id === './dag-hooks') {
      return {
        useCellUpdate: (...args) =>
          load(dagRoot + 'use-cell-update.ts').useCellUpdate(...args),
        useCustomCellBuilder: (...args) =>
          load(dagRoot + 'use-custom-cell-builder.ts').useCustomCellBuilder(
            ...args
          )
      }
    }
    if (id === '@/utils') {
      return {
        __esModule: true,
        default: { truncateText: (text, length) => text.slice(0, length) }
      }
    }
    if (id === '@/service/service') return { axios }
    if (id === '@/service/modules/task-instances') {
      return load('service/modules/task-instances/index.ts')
    }
    if (id === 'vue-i18n') return { useI18n: () => ({ t: (key) => key }) }
    if (id === 'vue-router') {
      return { useRoute: () => ({ params: { projectCode: '123' } }) }
    }
    if (id === '@/common/common') {
      return { parseTime: (value) => value, renderTableTime: () => '' }
    }
    if (!id.startsWith('.') && !id.startsWith('@/')) return require(id)
    const target = id.startsWith('@/')
      ? id.slice(2)
      : path.posix.join(path.posix.dirname(file), id)
    return load(target + (path.posix.extname(target) ? '' : '.ts'))
  }
  vm.runInThisContext(`(function(require,module,exports,window){${code}\n})`, {
    filename: file
  })(localRequire, module, module.exports, {
    $message: { success: (message) => messages.push(message) }
  })
  cache.set(file, module.exports)
  return module.exports
}

// Only storage/endpoints/attributes are modeled here. In particular this
// adapter never derives execution modes or edge styling from task types.
class Graph {
  constructor(json) {
    this.nodes = new Map()
    this.edges = []
    json.nodes.forEach((node) => this.addNode(node))
    json.edges.forEach((edge) => this.addEdge(edge))
  }
  on() {}
  addNode(metadata) {
    const node = cloneDeep(metadata)
    node.getData = () => node.data
    node.setData = (data) => Object.assign(node.data, data)
    node.getPosition = () => ({ x: node.x, y: node.y })
    node.attr = (key, value) => attribute(node, key, value)
    this.nodes.set(node.id, node)
    return node
  }
  addEdge(metadata) {
    const edge = cloneDeep(metadata)
    edge.getSourceCellId = () => edge.source.cell
    edge.getTargetCellId = () => edge.target.cell
    edge.getSourceNode = () => this.getCellById(edge.source.cell)
    edge.getTargetNode = () => this.getCellById(edge.target.cell)
    edge.getLabels = () => edge.labels || []
    edge.attr = (key, value) => attribute(edge, key, value)
    this.edges.push(edge)
    return edge
  }
  removeEdge(edge) {
    this.edges = this.edges.filter((item) => item !== edge)
  }
  getCellById(id) {
    return this.nodes.get(id)
  }
  getConnectedEdges(node) {
    return this.edges.filter(
      (edge) => edge.source.cell === node.id || edge.target.cell === node.id
    )
  }
  getNodes() {
    return [...this.nodes.values()]
  }
  getEdges() {
    return this.edges
  }
}
function attribute(cell, key, value) {
  const parts = key.split('/')
  if (value === undefined) return get(cell.attrs, parts)
  set(cell.attrs, parts, value)
}

const { buildGraph } = load(
  dagRoot + 'use-custom-cell-builder.ts'
).useCustomCellBuilder()
const { getConnects, getLocations } = load(
  dagRoot + 'use-business-mapper.ts'
).useBusinessMapper()
const { formatModel } = load(
  'views/projects/task/components/node/format-data.ts'
)

function task(code, taskType = 'SHELL', taskExecuteType = 'BATCH') {
  return {
    code,
    id: code + 100,
    version: 7,
    name: `task-${code}`,
    taskType,
    taskExecuteType,
    flag: 'YES',
    taskParams: {
      useCustom: true,
      startupScript: 'seatunnel.sh',
      rawScript: 'env { job.mode = "BATCH" }',
      localParams: [{ prop: 'key', value: 'source' }],
      resourceList: []
    }
  }
}
function makeEditor(tasks, connections = []) {
  const definition = vue.ref({
    workflowDefinition: { locations: '[]' },
    taskDefinitionList: tasks,
    workflowTaskRelationList: connections.map(
      ([preTaskCode, postTaskCode]) => ({
        preTaskCode,
        postTaskCode
      })
    )
  })
  const graph = new Graph(buildGraph(definition.value))
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
        editor = load(dagRoot + 'use-task-edit.ts').useTaskEdit({
          graph: vue.shallowRef(graph),
          definition
        })
        return () => null
      }
    })
  app.mount({})
  return { editor, graph, definition, close: () => app.unmount() }
}
function confirm(fixture, mode, preTasks = [10], code = 20) {
  fixture.editor.editTask(code)
  const model = formatModel(fixture.editor.currTask.value)
  model.taskExecuteType = mode
  model.preTasks = preTasks
  fixture.editor.taskConfirm({ data: model })
  assert.equal(fixture.editor.taskModalVisible.value, false)
}
function reload(fixture) {
  const { graph, definition } = fixture
  // The workflow save payload uses these production mappers and task list.
  // JSON round-tripping represents persistence without calling an API.
  const saved = JSON.parse(
    JSON.stringify({
      workflowDefinition: {
        locations: JSON.stringify(getLocations(graph.getNodes()))
      },
      taskDefinitionList: definition.value.taskDefinitionList,
      workflowTaskRelationList: getConnects(
        graph.getNodes(),
        graph.getEdges(),
        definition.value.taskDefinitionList
      )
    })
  )
  return { saved, graph: new Graph(buildGraph(saved)) }
}
function styles(graph) {
  return graph
    .getEdges()
    .map((edge) => [
      `${edge.getSourceCellId()}->${edge.getTargetCellId()}`,
      edge.attr('line/strokeDasharray')
    ])
    .sort(([a], [b]) => a.localeCompare(b))
}

const checks = []
async function check(name, run) {
  try {
    await run()
    checks.push({ name, result: 'PASS' })
  } catch (error) {
    checks.push({ name, result: 'FAIL', error: error.stack })
  }
}
async function main() {
  await check(
    'STREAM SeaTunnel copy preserves graph mode and independent parameters',
    () => {
      const fixture = makeEditor([task(20, 'SEATUNNEL', 'STREAM')])
      try {
        fixture.editor.copyTask('copied', 30, 20, 'SEATUNNEL', 'YES', {
          x: 100,
          y: 100
        })
        const [original, copied] = fixture.definition.value.taskDefinitionList
        assert.equal(copied.code, 30)
        assert.equal(copied.name, 'copied')
        assert.equal(copied.taskType, 'SEATUNNEL')
        assert.equal(copied.taskExecuteType, 'STREAM')
        assert.equal(original.taskExecuteType, 'STREAM')
        assert.equal(
          fixture.graph.getCellById('30').data.taskExecuteType,
          'STREAM'
        )
        assert.notEqual(copied.taskParams, original.taskParams)
        copied.taskParams.localParams[0].value = 'copy'
        assert.equal(original.taskParams.localParams[0].value, 'source')
        const persisted = reload(fixture)
        assert.equal(
          persisted.graph.getCellById('30').data.taskExecuteType,
          'STREAM'
        )
        assert.equal(
          persisted.saved.taskDefinitionList[1].taskExecuteType,
          'STREAM'
        )
        confirm(fixture, 'BATCH', [], 30)
        assert.equal(
          fixture.graph.getCellById('30').data.taskExecuteType,
          'BATCH'
        )
        assert.equal(
          fixture.graph.getCellById('20').data.taskExecuteType,
          'STREAM'
        )
        assert.equal(original.taskExecuteType, 'STREAM')
      } finally {
        fixture.close()
      }
    }
  )

  await check(
    'Confirm updates incoming and outgoing edges BATCH -> STREAM -> BATCH',
    () => {
      const fixture = makeEditor(
        [task(10), task(20, 'SEATUNNEL'), task(30), task(40), task(50)],
        [
          [10, 20],
          [20, 30],
          [40, 50]
        ]
      )
      try {
        const unrelated = fixture.graph.getEdges()[2]
        assert.deepEqual(styles(fixture.graph), [
          ['10->20', 'none'],
          ['20->30', 'none'],
          ['40->50', 'none']
        ])
        for (const [mode, dash] of [
          ['STREAM', '5 5'],
          ['BATCH', 'none']
        ]) {
          const incoming = fixture.graph
            .getEdges()
            .find((edge) => edge.getTargetCellId() === '20')
          confirm(fixture, mode)
          assert.equal(fixture.graph.getEdges().includes(incoming), false)
          assert.equal(fixture.graph.getEdges().includes(unrelated), true)
          assert.equal(
            fixture.graph.getCellById('20').data.taskExecuteType,
            mode
          )
          const expected = [
            ['10->20', dash],
            ['20->30', dash],
            ['40->50', 'none']
          ]
          assert.deepEqual(styles(fixture.graph), expected)
          const persisted = reload(fixture)
          assert.deepEqual(styles(persisted.graph), expected)
          assert.equal(
            persisted.saved.taskDefinitionList.find((item) => item.code === 20)
              .taskExecuteType,
            mode
          )
        }
      } finally {
        fixture.close()
      }
    }
  )

  for (const direction of ['incoming', 'outgoing']) {
    await check(
      `BATCH SeaTunnel retains dashed ${direction} edge to a STREAM neighbor`,
      () => {
        const fixture = makeEditor(
          [
            direction === 'incoming'
              ? task(10, 'FLINK_STREAM', 'STREAM')
              : task(10),
            task(20, 'SEATUNNEL', 'STREAM'),
            direction === 'outgoing'
              ? task(30, 'FLINK_STREAM', 'STREAM')
              : task(30)
          ],
          [
            [10, 20],
            [20, 30]
          ]
        )
        try {
          confirm(fixture, 'BATCH')
          const expected = [
            ['10->20', direction === 'incoming' ? '5 5' : 'none'],
            ['20->30', direction === 'outgoing' ? '5 5' : 'none']
          ]
          assert.deepEqual(styles(fixture.graph), expected)
          assert.deepEqual(styles(reload(fixture).graph), expected)
          const neighbor = direction === 'incoming' ? '10' : '30'
          assert.equal(
            fixture.graph.getCellById(neighbor).data.taskExecuteType,
            'STREAM'
          )
        } finally {
          fixture.close()
        }
      }
    )
  }

  await check(
    'Legacy FLINK_STREAM null mode remains dashed after Confirm and reload',
    () => {
      const fixture = makeEditor(
        [task(10), task(20, 'FLINK_STREAM', null), task(30)],
        [
          [10, 20],
          [20, 30]
        ]
      )
      try {
        confirm(fixture, null)
        assert.equal(
          fixture.graph.getCellById('20').data.taskExecuteType,
          'STREAM'
        )
        const expected = [
          ['10->20', '5 5'],
          ['20->30', '5 5']
        ]
        assert.deepEqual(styles(fixture.graph), expected)
        const persisted = reload(fixture)
        assert.deepEqual(styles(persisted.graph), expected)
        assert.equal(
          persisted.saved.taskDefinitionList[1].taskExecuteType,
          null
        )
      } finally {
        fixture.close()
      }
    }
  )

  const table = load(
    'views/projects/task/instance/use-stream-table.ts'
  ).useTable()
  table.createColumns(table.variables)
  const operation = table.variables.columns.find(
    (column) => column.key === 'operation'
  )
  function savepoint(taskType) {
    const tooltips = operation.render({ id: 456, taskType }).children.default()
    const matches = tooltips.filter(
      (tooltip) => tooltip.children.default() === 'project.task.savepoint'
    )
    assert.equal(matches.length, 1)
    const button = matches[0].children.trigger()
    assert.equal(button.type, NButton)
    return button
  }
  await check('SeaTunnel savepoint renderer disables its button', () => {
    assert.equal(savepoint('SEATUNNEL').props.disabled, true)
    assert.equal(requests.length, 0)
  })
  await check('Flink savepoint renderer keeps its button enabled', () => {
    assert.equal(savepoint('FLINK_STREAM').props.disabled, false)
    assert.equal(requests.length, 0)
  })
  await check(
    'Enabled Flink savepoint calls the correct endpoint and refreshes Stream tasks',
    async () => {
      // Calling a disabled VNode callback would bypass Naive UI's click guard.
      // Only invoke the enabled callback; disabled DOM interaction is an E2E concern.
      const button = savepoint('FLINK_STREAM')
      assert.equal(button.props.disabled, false)
      button.props.onClick()
      await new Promise((resolve) => setImmediate(resolve))
      assert.equal(requests.length, 2)
      assert.deepEqual(requests[0], {
        url: 'projects/123/task-instances/456/savepoint',
        method: 'post'
      })
      assert.equal(requests[1].url, '/projects/123/task-instances')
      assert.equal(requests[1].method, 'get')
      assert.equal(requests[1].params.taskExecuteType, 'STREAM')
      assert.deepEqual(messages, ['project.task.success'])
    }
  )

  for (const item of checks) {
    process.stdout.write(
      `${item.result} ${item.name}${item.error ? ': ' + item.error : ''}\n`
    )
  }
  process.exitCode = checks.some((item) => item.result === 'FAIL') ? 1 : 0
}
main().catch((error) => {
  process.stderr.write(`${error.stack}\n`)
  process.exitCode = 1
})

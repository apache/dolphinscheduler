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
<template>
  <div class="list-model" style="position: relative">
    <div class="table-box">
      <el-table
        :data="list"
        size="mini"
        style="width: 100%"
        @selection-change="select"
      >
        <el-table-column :label="$t('Task Name')" min-width="200">
          <template v-slot="scope">
            <el-popover trigger="hover" placement="top">
              <div>{{ scope.row.taskName }}</div>
              <div slot="reference" class="name-wrapper">
                <a
                  href="javascript:"
                  class="links"
                  @click="viewTaskDetail(scope.row)"
                >
                  <span class="ellipsis name">{{ scope.row.taskName }}</span>
                </a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('Process Name')"
          prop="processDefinitionName"
          min-width="135"
        >
        </el-table-column>
        <el-table-column
          :label="$t('Process State')"
          prop="processReleaseState"
          min-width="135"
        >
        </el-table-column>
        <el-table-column
          :label="$t('Task Type')"
          prop="taskType"
          min-width="135"
        >
        </el-table-column>
        <el-table-column :label="$t('Version Info')" min-width="135">
          <template v-slot="scope">
            <span>
              {{ "V" + scope.row.taskVersion }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Upstream Tasks')" min-width="300">
          <template v-slot="scope">
            <div class="upstream-tasks">
              <el-popover
                trigger="hover"
                placement="top"
                v-for="task in scope.row.upstreamTasks.slice(0, 3)"
                :key="task.taskCode"
              >
                <div>{{ task.taskName }}</div>
                <el-tag class="pre-task-tag" size="mini" slot="reference">
                  {{ task.taskName }}
                </el-tag>
              </el-popover>
              <!-- more popover -->
              <el-popover
                v-if="scope.row.upstreamTasks.length > 3"
                trigger="hover"
                :title="$t('Upstream Tasks')"
                placement="top"
              >
                <div class="task-definition-upstreams-popover">
                  <el-tag
                    size="mini"
                    slot="reference"
                    class="popover-tag"
                    v-for="task in scope.row.upstreamTasks"
                    :key="task.taskCode"
                  >
                    {{ task.taskName }}
                  </el-tag>
                </div>
                <el-tag class="pre-task-tag" size="mini" slot="reference">
                  {{
                    $t("and {n} more", {
                      n: scope.row.upstreamTasks.length - 3,
                    })
                  }}
                </el-tag>
              </el-popover>
              <span v-if="scope.row.upstreamTasks.length === 0">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Create Time')" min-width="135">
          <template v-slot="scope">
            <span>
              {{ scope.row.taskCreateTime | formatDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="135">
          <template v-slot="scope">
            <span>
              {{ scope.row.taskUpdateTime | formateDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="150" fixed="right">
          <template v-slot="scope">
            <el-tooltip
              :content="$t('Edit')"
              placement="top"
              :enterable="false"
            >
              <span>
                <el-button
                  type="primary"
                  size="mini"
                  icon="el-icon-edit-outline"
                  circle
                  :disabled="
                    ['CONDITIONS', 'SWITCH'].includes(scope.row.taskType) ||
                    (scope.row.processDefinitionCode &&
                      scope.row.processReleaseState === 'ONLINE')
                  "
                  @click="editTask(scope.row)"
                ></el-button>
              </span>
            </el-tooltip>
            <el-tooltip
              :content="$t('Move task')"
              placement="top"
              :enterable="false"
            >
              <span>
                <el-button
                  type="primary"
                  size="mini"
                  icon="el-icon-rank"
                  circle
                  :disabled="
                    scope.row.processDefinitionCode &&
                    scope.row.processReleaseState === 'ONLINE'
                  "
                  @click="showMoveModal(scope.row)"
                ></el-button>
              </span>
            </el-tooltip>
            <el-tooltip
              :content="$t('Delete')"
              placement="top"
              :enterable="false"
            >
              <span>
                <el-button
                  type="danger"
                  size="mini"
                  icon="el-icon-delete"
                  slot="reference"
                  circle
                  :disabled="
                    scope.row.processDefinitionCode &&
                    scope.row.processReleaseState === 'ONLINE'
                  "
                  @click="showDeleteModal(scope.row)"
                ></el-button>
              </span>
            </el-tooltip>
            <el-tooltip
              :content="$t('Version Info')"
              placement="top"
              :enterable="false"
            >
              <span
                ><el-button
                  type="primary"
                  size="mini"
                  icon="el-icon-info"
                  @click="viewTaskVersions(scope.row)"
                  circle
                ></el-button
              ></span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
  import _ from 'lodash'

  export default {
    name: 'task-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      tasksList: Array
    },
    watch: {
      tasksList: {
        handler (a) {
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(a)
          })
        },
        immediate: true,
        deep: true
      }
    },
    created () {},
    methods: {
      /**
       * Delete task
       */
      showDeleteModal (taskRow) {
        this.$emit('showDeleteModal', taskRow)
      },
      /**
       * View task detail
       */
      viewTaskDetail (taskRow) {
        this.$emit('viewTaskDetail', taskRow)
      },
      /**
       * Edit task
       */
      editTask (taskRow) {
        this.$emit('editTask', taskRow)
      },
      /**
       * View task versions
       */
      viewTaskVersions (taskRow) {
        this.$emit('viewTaskVersions', taskRow)
      },
      /**
       * Move Task
       */
      showMoveModal (taskRow) {
        this.$emit('showMoveModal', taskRow)
      }
    }
  }
</script>

<style lang="scss">
.task-definition-upstreams-popover {
  max-width: 500px;
  .popover-tag {
    margin-right: 10px;
    margin-bottom: 10px;
  }
}
</style>

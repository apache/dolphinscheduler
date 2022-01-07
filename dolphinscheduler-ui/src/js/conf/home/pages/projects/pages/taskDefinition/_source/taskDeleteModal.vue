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
  <div>
    <el-dialog :title="$t('Delete')" :visible.sync="visible" width="500px">
      <div class="content" v-if="taskRow">
        <template v-if="taskRow.processDefinitionCode">
          <span>{{
            $t("Delete task {taskName} from process {processName}?", {
              processName: taskRow.processDefinitionName,
              taskName: taskRow.taskName,
            })
          }}</span>
          <el-checkbox class="remove-checkbox" v-model="removeCompletely">{{
            $t("Delete task completely")
          }}</el-checkbox>
        </template>
        <template v-else>
          <span>{{
            $t("Delete {taskName}?", {
              taskName: taskRow.taskName,
            })
          }}</span>
        </template>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="close">{{ $t("Cancel") }}</el-button>
        <el-button size="small" type="primary" @click="submit">{{
          $t("Confirm")
        }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    props: {
      taskRow: Object
    },
    data () {
      return {
        visible: false,
        // Whether to delete the task completely
        removeCompletely: false
      }
    },
    methods: {
      show () {
        this.visible = true
      },
      close () {
        this.visible = false
      },
      submit () {
        this.$emit('deleteTask', {
          completely: this.taskRow.processDefinitionCode ? this.removeCompletely : true,
          taskCode: this.taskRow.taskCode,
          processDefinitionCode: this.taskRow.processDefinitionCode
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
.content {
  margin: 20px;
  color: #333;
  display: flex;
  flex-direction: column;

  .remove-checkbox {
    margin-top: 20px;
  }
}
</style>

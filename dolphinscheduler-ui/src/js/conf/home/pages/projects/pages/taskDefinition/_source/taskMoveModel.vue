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
    <el-dialog :title="$t('Move task')" :visible.sync="visible" width="500px">
      <div class="content" v-if="taskRow">
        <el-form ref="form" :model="form" label-width="100px" size="mini">
          <el-form-item :label="$t('Process Name')">
            <el-select v-model="form.processCode">
              <el-option
                :label="process.name"
                :value="process.code"
                v-for="process in processListS"
                :key="process.code"
                >{{ process.name }}</el-option
              >
            </el-select>
          </el-form-item>
        </el-form>
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
  import { mapState } from 'vuex'

  export default {
    props: {
      taskRow: Object
    },
    data () {
      return {
        visible: false,
        form: {
          processCode: -1
        }
      }
    },
    computed: {
      ...mapState('dag', ['processListS'])
    },
    methods: {
      show () {
        this.visible = true
      },
      close () {
        this.visible = false
      },
      submit () {
        if (this.taskRow.processDefinitionCode === this.form.processCode) {
          this.visible = false
          return
        }
        if (!this.form.processCode) {
          this.$message.error(this.$t('Please select a process (required)'))
          return
        }
        this.$emit('moveTask', {
          taskCode: this.taskRow.taskCode,
          processDefinitionCode: this.taskRow.processDefinitionCode,
          targetProcessDefinitionCode: this.form.processCode
        })
      }
    },
    watch: {
      taskRow (val) {
        if (val) {
          this.form.processCode = val.processDefinitionCode || ''
        }
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

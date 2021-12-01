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
  <el-dialog
    :title="$t('Format DAG')"
    :visible.sync="visible"
    width="500px"
    class="dag-layout-modal"
    :append-to-body="true"
  >
    <el-form
      ref="form"
      :model="form"
      label-width="100px"
      class="dag-layout-form"
    >
      <el-form-item :label="$t('layoutType')">
        <el-radio-group v-model="form.type">
          <el-radio label="grid">{{ $t("gridLayout") }}</el-radio>
          <el-radio label="dagre">{{ $t("dagreLayout") }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('rows')" v-if="form.type === LAYOUT_TYPE.GRID">
        <el-input-number
          v-model="form.rows"
          :min="0"
          size="small"
        ></el-input-number>
      </el-form-item>
      <el-form-item :label="$t('cols')" v-if="form.type === LAYOUT_TYPE.GRID">
        <el-input-number
          v-model="form.cols"
          :min="0"
          size="small"
        ></el-input-number>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="close">{{ $t("Cancel") }}</el-button>
      <el-button size="small" type="primary" @click="submit">{{
        $t("Confirm")
      }}</el-button>
    </span>
  </el-dialog>
</template>
<script>
  export const LAYOUT_TYPE = {
    GRID: 'grid',
    DAGRE: 'dagre'
  }

  export const DEFAULT_LAYOUT_CONFIG = {
    cols: 0,
    nodesep: 50,
    padding: 50,
    ranksep: 50,
    rows: 0,
    type: LAYOUT_TYPE.DAGRE
  }

  export default {
    data () {
      return {
        visible: false,
        form: { ...DEFAULT_LAYOUT_CONFIG },
        LAYOUT_TYPE
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
        this.$emit('submit', this.form)
        this.close()
      }
    }
  }
</script>
<style lang="scss" scoped>
.dag-layout-modal {
  ::v-deep .el-dialog__header {
    border-bottom: solid 1px #d4d4d4;
  }

  ::v-deep .dag-layout-form {
    margin-top: 20px;
  }

  ::v-deep .el-radio {
    margin-bottom: 0;
  }

  .el-form-item {
    margin-bottom: 10px;
  }
}
</style>

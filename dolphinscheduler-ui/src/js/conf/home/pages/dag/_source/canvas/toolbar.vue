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
  <div class="dag-toolbar">
    <h3>{{ dagChart.name || $t("Create process") }}</h3>
    <el-tooltip
      v-if="dagChart.name"
      class="toolbar-operation"
      :content="$t('Copy name')"
      placement="bottom"
    >
      <i class="el-icon-copy-document" @click="copyName"></i>
    </el-tooltip>
    <textarea ref="textarea" cols="30" rows="10" class="transparent"></textarea>
    <div class="toolbar-left">
      <el-tooltip
        :content="$t('View variables')"
        placement="bottom"
        class="toolbar-operation"
      >
        <i
          class="custom-ico view-variables"
          v-if="$route.name === 'projects-instance-details'"
          @click="toggleVariableView"
        ></i>
      </el-tooltip>
      <el-tooltip
        :content="$t('Startup parameter')"
        placement="bottom"
        class="toolbar-operation"
      >
        <i
          class="custom-ico startup-parameters"
          v-if="$route.name === 'projects-instance-details'"
          @click="toggleParamView"
        ></i>
      </el-tooltip>
    </div>
    <div class="toolbar-right">
      <el-tooltip
        class="toolbar-operation"
        :content="$t('Delete selected lines or nodes')"
        placement="bottom"
        v-if="!isDetails"
      >
        <i class="el-icon-delete" @click="removeCells"></i>
      </el-tooltip>
      <el-tooltip
        class="toolbar-operation"
        :content="$t('Download')"
        placement="bottom"
      >
        <i class="el-icon-download" @click="downloadPNG"></i>
      </el-tooltip>
      <el-tooltip
        class="toolbar-operation"
        :content="$t('Full Screen')"
        placement="bottom"
      >
        <i
          :class="[
            'custom-ico',
            dagChart.fullScreen ? 'full-screen-close' : 'full-screen-open',
          ]"
          @click="toggleFullScreen"
        ></i>
      </el-tooltip>
      <el-tooltip
        class="toolbar-operation"
        :content="$t('Refresh DAG status')"
        placement="bottom"
        v-if="dagChart.type === 'instance'"
      >
        <i class="el-icon-refresh" @click="refreshTaskStatus"></i>
      </el-tooltip>
      <el-tooltip
        class="toolbar-operation last"
        :content="$t('Format DAG')"
        placement="bottom"
      >
        <i class="custom-ico graph-format" @click="chartFormat"></i>
      </el-tooltip>
      <el-button
        class="toolbar-el-btn"
        type="primary"
        size="mini"
        v-if="dagChart.type === 'definition'"
        @click="showVersions"
        icon="el-icon-info"
        >{{$t('Version Info')}}</el-button
      >
      <el-button
        class="toolbar-el-btn"
        type="primary"
        size="mini"
        @click="saveProcess"
        >{{ $t("Save") }}</el-button
      >
      <el-button
        class="toolbar-el-btn"
        v-if="$route.query.subProcessCodes"
        type="primary"
        size="mini"
        icon="el-icon-back"
        @click="dagChart.returnToPrevProcess"
      >
        {{ $t("Return_1") }}
      </el-button>
      <el-button
        class="toolbar-el-btn"
        type="primary"
        icon="el-icon-switch-button"
        size="mini"
        v-if="type === 'instance' || 'definition'"
        @click="returnToListPage"
      >
        {{ $t("Close") }}
      </el-button>
    </div>
  </div>
</template>

<script>
  import { findComponentDownward } from '@/module/util/'
  import { mapState } from 'vuex'

  export default {
    name: 'dag-toolbar',
    inject: ['dagChart'],
    data () {
      return {
        canvasRef: null
      }
    },
    computed: {
      ...mapState('dag', [
        'isDetails'
      ])
    },
    methods: {
      getDagCanvasRef () {
        if (this.canvasRef) {
          return this.canvasRef
        } else {
          const canvas = findComponentDownward(this.dagChart, 'dag-canvas')
          this.canvasRef = canvas
          return canvas
        }
      },
      toggleVariableView () {
        findComponentDownward(this.$root, 'assist-dag-index')._toggleView()
      },
      toggleParamView () {
        findComponentDownward(
          this.$root,
          'starting-params-dag-index'
        )._toggleParam()
      },
      toggleFullScreen () {
        this.dagChart.toggleFullScreen()
      },
      saveProcess () {
        const canvas = this.getDagCanvasRef()
        const nodes = canvas.getNodes()
        if (!nodes.length) {
          this.$message.error(this.$t('Failed to create node to save'))
          return
        }
        this.dagChart.toggleSaveDialog(true)
      },
      downloadPNG () {
        const canvas = this.getDagCanvasRef()
        canvas.downloadPNG(this.processName)
      },
      removeCells () {
        const canvas = this.getDagCanvasRef()
        const selections = canvas.getSelections()
        canvas.removeCells(selections)
      },
      copyName () {
        const textarea = this.$refs.textarea
        textarea.value = this.dagChart.name
        textarea.select()
        document.execCommand('copy')
        this.$message(this.$t('Copy success'))
      },
      chartFormat () {
        const canvas = this.getDagCanvasRef()
        canvas.format()
      },
      refreshTaskStatus () {
        this.dagChart.refreshTaskStatus()
      },
      returnToListPage () {
        let $name = this.$route.name
        if ($name && $name.indexOf('definition') !== -1) {
          this.$router.push({ name: 'projects-definition-list' })
        } else {
          this.$router.push({ name: 'projects-instance-list' })
        }
      },
      showVersions () {
        this.dagChart.showVersions()
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./toolbar";
</style>

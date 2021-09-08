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
  <el-drawer
    :visible.sync="drawerVisible"
    :wrapperClosable="false"
    size=""
    :with-header="false"
  >
    <div class="form-model-wrapper">
      <div class="title-box">
        <span class="name">{{ $t("Current connection settings") }}</span>
      </div>
      <div class="content-box">
        <div class="form-model">
          <!-- Node name -->
          <div class="clearfix list">
            <div class="text-box">
              <span>{{ $t("Connection name") }}</span>
            </div>
            <div class="cont-box">
              <label class="label-box">
                <el-input
                  type="text"
                  size="small"
                  v-model="label"
                  :disabled="isDetails"
                  :placeholder="$t('Please enter name')"
                  maxlength="100"
                >
                </el-input>
              </label>
            </div>
          </div>
        </div>
      </div>
      <div class="bottom-box">
        <div class="submit" style="background: #fff">
          <el-button type="text" size="small" @click="cancel()">
            {{ $t("Cancel") }}
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="ok()"
            :disabled="isDetails"
            >{{ $t("Confirm add") }}
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  import { mapState } from 'vuex'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'edge-edit-model',
    data () {
      return {
        id: '',
        label: '',
        drawerVisible: false
      }
    },
    inject: ['dagChart'],
    mixins: [disabledState],
    computed: {
      ...mapState('dag', ['isDetails'])
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
      show ({ id, label }) {
        this.id = id
        this.label = label
        this.drawerVisible = true
      },
      cancel () {
        this.drawerVisible = false
        this.id = ''
        this.label = ''
      },
      ok () {
        const canvas = this.getDagCanvasRef()
        canvas.setEdgeLabel(this.id, this.label)
        this.cancel()
      }
    }
  }
</script>

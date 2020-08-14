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
  <div class="home-main index-model">
    <m-dag v-if="!isLoading" :type="'definition'" :release-state="releaseState"></m-dag>
    <m-spin :is-spin="isLoading" ></m-spin>
  </div>
</template>
<script>
  import mDag from './_source/dag.vue'
  import mSpin from '@/module/components/spin/spin'
  import Affirm from './_source/jumpAffirm'
  import disabledState from '@/module/mixin/disabledState'
  import { mapActions, mapMutations } from 'vuex'

  export default {
    name: 'definition-details',
    data () {
      return {
        // loading
        isLoading: true,
        // state
        releaseState: ''
      }
    },
    mixins: [disabledState],
    props: {},
    methods: {
      ...mapMutations('dag', ['resetParams', 'setIsDetails']),
      ...mapActions('dag', ['getProcessList','getProjectList', 'getResourcesList', 'getProcessDetails','getResourcesListJar']),
      ...mapActions('security', ['getTenantList','getWorkerGroupsAll']),
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Initialization parameters
        this.resetParams()
        // Promise Get node needs data
        Promise.all([
          // Node details
          this.getProcessDetails(this.$route.params.id),
          // get process definition
          this.getProcessList(),
          // get project
          this.getProjectList(),
          // get resource
          this.getResourcesList(),
          // get jar
          this.getResourcesListJar(),
          this.getResourcesListJar('PYTHON'),
          // get worker group list
          this.getWorkerGroupsAll(),
          this.getTenantList()
        ]).then((data) => {
          let item = data[0]
          this.setIsDetails(item.releaseState === 'ONLINE')
          this.releaseState = item.releaseState
          this.isLoading = false
          // Whether to pop up the box?
          Affirm.init(this.$root)
        }).catch(() => {
          this.isLoading = false
        })
      },
      /**
       * Redraw (refresh operation)
       */
      _reset () {
        this.getProcessDetails(this.$route.params.id).then(res => {
          let item = res
          this.setIsDetails(item.releaseState === 'ONLINE')
          this.releaseState = item.releaseState
        })
      }
    },
    watch: {
      // Listening for routing changes
      '$route': {
        deep: true,
        handler () {
          this.init()
        }
      }
    },
    created () {
      this.init()
    },
    mounted () {
    },
    components: { mDag, mSpin }
  }
</script>

/* * Licensed to the Apache Software Foundation (ASF) under one or more *
contributor license agreements. See the NOTICE file distributed with * this work
for additional information regarding copyright ownership. * The ASF licenses
this file to You under the Apache License, Version 2.0 * (the "License"); you
may not use this file except in compliance with * the License. You may obtain a
copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless
required by applicable law or agreed to in writing, software * distributed under
the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. * See the License for the
specific language governing permissions and * limitations under the License. */
<template>
  <div>
    <m-list-box>
      <div slot="text">{{ $t('Namespace') }}</div>
      <div slot="content">
        <el-select
          size="small"
          v-model="namespace"
          :loading="namespacesLoading"
          style="width: 300px"
          :disabled="isDetails"
        >
          <el-option-group>
            <div class="select-title">
              <span>{{ $t('Namespace') }}</span>
              <span>{{ $t('Cluster') }}</span>
            </div>
            <el-option
              v-for="item in namespaces"
              :key="item.id"
              :label="item.namespace + ' (' + item.k8s + ')'"
              :value="item.id"
            >
              <span style="float: left; margin-right: 30px">{{
                item.namespace
              }}</span>
              <span style="float: right">{{ item.k8s }}</span>
            </el-option>
          </el-option-group>
        </el-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{ $t('Min cpu') }}</div>
      <div slot="content">
        <el-input
          v-model="minCpuCores"
          size="small"
          :placeholder="$t('Please enter min cpu')"
          style="width: 160px"
          :disabled="isDetails"
        >
        </el-input>
        <span>(Core)</span>
        <span class="text-b">{{ $t('Min memory') }}</span>
        <el-input
          size="small"
          v-model="minMemorySpace"
          :placeholder="$t('Please enter min memory')"
          style="width: 160px"
          :disabled="isDetails"
        >
        </el-input>
        <span>(MB)</span>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{ $t('Image') }}</div>
      <div slot="content">
        <el-input
          type="input"
          size="small"
          v-model="image"
          :placeholder="$t('Please enter image')"
          :disabled="isDetails"
        >
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{ $t('Custom Parameters') }}</div>
      <div slot="content">
        <m-local-params
          ref="refLocalParams"
          @on-local-params="_onLocalParams"
          :udp-list="localParams"
          :hide="false"
        >
        </m-local-params>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mListBox from './_source/listBox'
  import disabledState from '@/module/mixin/disabledState'
  import mLocalParams from './_source/localParams'
  import { mapActions } from 'vuex'

  export default {
    name: 'k8s',
    data() {
      return {
        namespace: '',
        minCpuCores: '',
        minMemorySpace: '',
        mainClass: '',
        image: '',
        // Custom parameter
        localParams: [],
        namespaces: [],
        namespacesLoading: true
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      isNewCreate: Object,
      jobName: String
    },
    methods: {
      ...mapActions('security', ['getAvailableNamespaceListP']),
      /**
       * return localParams
       */
      _onLocalParams(a) {
        this.localParams = a
      },
      /**
       * verification
       */
      _verification() {
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        const nsAndCluster = this._getNamespaceAndCluster()
        // storage
        this.$emit('on-params', {
          namespace: nsAndCluster.namespace,
          clusterName: nsAndCluster.clusterName,
          minCpuCores: this.minCpuCores,
          minMemorySpace: this.minMemorySpace,
          image: this.image,
          localParams: this.localParams,
          mainClass: this.mainClass
        })
        return true
      },
      _cacheParams() {
        const nsAndCluster = this._getNamespaceAndCluster()
        this.$emit('on-cache-params', {
          namespace: nsAndCluster.namespace,
          clusterName: nsAndCluster.clusterName,
          minCpuCores: this.minCpuCores,
          minMemorySpace: this.minMemorySpace,
          image: this.image,
          localParams: this.localParams,
          mainClass: this.mainClass
        })
      },
      _getNamespaces() {
        return this.getAvailableNamespaceListP()
          .then((options) => {
            this.namespaces = options
          })
          .catch(() => {
            this.namespaces = []
          })
          .finally(() => {
            this.namespacesLoading = false
          })
      },
      _getNamespaceAndCluster() {
        const res = this.namespaces.find((item) => item.id === this.namespace)
        return res
          ? {
              namespace: res.namespace,
              clusterName: res.k8s
            }
          : {}
      }
    },
    watch: {
      // Watch the cacheParams
      cacheParams() {
        this._cacheParams()
      }
    },
    created() {
      let o = this.backfillItem
      const promise = this._getNamespaces()
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        const params = _.cloneDeep(o.params)
        if (params.namespace && params.clusterName) {
          promise.then(() => {
            const ns = this.namespaces.find(
              (item) =>
                item.namespace === params.namespace &&
                item.k8s === params.clusterName
            )
            ns && (this.namespace = ns.id)
          })
        }
        this.minCpuCores = params.minCpuCores || ''
        this.minMemorySpace = params.minMemorySpace || ''
        this.image = params.image || undefined
        this.localParams = params.localParams || []
        this.mainClass = params.mainClass || ''
      }
    },
    computed: {
      cacheParams() {
        const nsAndCluster = this._getNamespaceAndCluster()
        return {
          namespace: nsAndCluster.namespace,
          clusterName: nsAndCluster.clusterName,
          minCpuCores: this.minCpuCores,
          minMemorySpace: this.minMemorySpace,
          image: this.image,
          localParams: this.localParams,
          mainClass: this.mainClass
        }
      }
    },
    components: { mListBox, mLocalParams }
  }
</script>
<style lang="scss" scoped>
  /deep/ .select-title {
    height: 30px;
    padding: 0 20px;
    overflow: hidden;
    font-size: 12px;
    color: #777;
    line-height: 30px;
    > :first-child {
      float: left;
    }
    > :last-child {
      float: right;
    }
  }
</style>

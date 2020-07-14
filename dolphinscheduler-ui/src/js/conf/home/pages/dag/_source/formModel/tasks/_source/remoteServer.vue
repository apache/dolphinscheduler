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
  <div class="datasource-model">
    <div class="select-listpp">
      <x-switch v-model="remote" :disabled="isDetails"></x-switch>
      <x-select
        v-if="remote"
        :placeholder="$t('Please select the remote server')"
        v-model="datasource"
        style="width: 288px;"
        :disabled="isDetails"
      >
        <x-option v-for="city in datasourceList" :key="city.id" :value="city.id" :label="city.code"></x-option>
      </x-select>
    </div>
  </div>
</template>
<script>
import _ from "lodash";
import i18n from "@/module/i18n";
import disabledState from "@/module/mixin/disabledState";

export default {
  name: "remote-server",
  data() {
    return {
      type: "REMOTESERVER",
      // remote exec status
      remote: false,
      // data source
      datasource: "",
      // data source(List)
      datasourceList: []
    };
  },
  mixins: [disabledState],
  props: {
    data: Object,
    supportType: Array
  },
  methods: {
    /**
     * Get the corresponding datasource data according to type
     */
    _getDatasourceData() {
      return new Promise((resolve, reject) => {
        this.store.dispatch("dag/getDatasourceList", this.type).then(res => {
          this.datasourceList = _.map(res.data, v => {
            return {
              id: v.id,
              code: v.name,
              disabled: false
            };
          });
          resolve();
        });
      });
    }
  },
  computed: {
    cacheParams() {
      return {
        remote: this.remote,
        datasource: this.datasource
      };
    }
  },
  // Watch the cacheParams
  watch: {
    remote(val) {
      if (val) {
        this._getDatasourceData().then(res => {
          this.datasource = (this.datasourceList.length && this.datasourceList[0].id) || "";
          this.$emit("on-dsData", {
            type: this.type,
            datasource: this.datasource
          });
        });
      } else {
         this.$emit("on-dsData", {
          remote: this.remote,
          datasource: ""
        });
      }
    },
    datasource(val) {
      this.$emit("on-dsData", {
        remote: this.remote,
        datasource: val
      });
    }
  },
  created() {
    this.$emit("on-dsData", {
      remote: this.remote,
      datasource: this.datasource
    });
  },
  mounted() {},
  components: {}
};
</script>

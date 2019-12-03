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
  <div class="affirm-model">
    <m-popup :ok-text="$t('Save')"
            :nameText="$t('Whether to save the DAG graph')"
            @close="_close"
            @ok="_ok">
    </m-popup>
  </div>
</template>
<script>
  import mPopup from '@/module/components/popup/popup'

  export default {
    name: 'affirm',
    methods: {
      _ok () {
        this.$emit('ok')
      },
      _close () {
        this.$emit('close')
      }
    },
    components: { mPopup }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .affirm-model {
    .popup-model  {
      .top-p {
        height: 50px;
      }
      .content-p {
        min-height: 0px;
        min-width: 250px;
      }
    }
  }
</style>

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

import Vue from 'vue'
import mAffirm from './jumpAffirm'
import store from '@/conf/home/store'
import router from '@/conf/home/router'
import { uuid, findComponentDownward } from '@/module/util/'

const Affirm = {}
let $root = {}
let $routerType = ''
let $isPop = true

/**
 * Listen for route changes
 */
router.beforeEach((to, from, next) => {
  if (from.name === 'projects-definition-details' || from.name === 'projects-instance-details' || from.name === 'definition-create') {
    if (!Affirm.paramVerification(from.name)) {
      Affirm.isPop(() => {
        next()
      })
    } else {
      next()
    }
  } else {
    next()
  }
})

/**
 * Get judgment initialization data
 */
Affirm.init = (root) => {
  $isPop = true
  $root = root
  $routerType = router.history.current.name
}

/**
 * Parameter verification
 */
Affirm.paramVerification = (name) => {
  if (!$isPop) {
    return true
  }
  const dagStore = store.state.dag
  let flag = false
  if ($routerType === 'definition-create') {
    // No nodes jump out directly
    if (dagStore.tasks.length) {
      if (!dagStore.name) {
        store.commit('dag/setName', `${uuid('dag_')}${uuid() + uuid()}`)
      }
    } else {
      flag = true
    }
  } else {
    // View history direct jump
    flag = name === 'projects-instance-details' ? true : !dagStore.isEditDag
  }
  return flag
}

/**
 * Pop-up judgment
 */
Affirm.isPop = (fn) => {
  Vue.$modal.dialog({
    closable: false,
    showMask: true,
    escClose: true,
    className: 'v-modal-custom',
    transitionName: 'opacityp',
    render (h) {
      return h(mAffirm, {
        on: {
          ok () {
            // save
            findComponentDownward($root, 'dag-chart')._save('affirm').then(() => {
              fn()
              Vue.$modal.destroy()
            }).catch(() => {
              fn()
              Vue.$modal.destroy()
            })
          },
          close () {
            fn()
            Vue.$modal.destroy()
          }
        },
        props: {
        }
      })
    }
  })
}

/**
 * Whether the external setting pops up
 */
Affirm.setIsPop = (is) => {
  $isPop = is
}

export default Affirm

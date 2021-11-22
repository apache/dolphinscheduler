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
import Router from 'vue-router'
import home from './module/home'
import datasource from './module/datasource'
import monitor from './module/monitor'
import projects from './module/projects'
import resource from './module/resource'
import security from './module/security'
import user from './module/user'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: {
        name: 'home'
      }
    },
    ...home,
    ...projects,
    ...resource,
    ...datasource,
    ...security,
    ...user,
    ...monitor
  ]
})

const VueRouterPush = Router.prototype.push
Router.prototype.push = function push (to) {
  return VueRouterPush.call(this, to).catch(err => err)
}

router.beforeEach((to, from, next) => {
  const $body = $('body')
  $body.find('.tooltip.fade.top.in').remove()
  if (to.meta.title) {
    document.title = `${to.meta.title} - DolphinScheduler`
  }
  next()
})

export default router

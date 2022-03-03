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

import i18n from '@/module/i18n/index.js'

const dataquality = [
  {
    path: '/dataquality',
    name: 'dataquality',
    component: resolve => require(['../../pages/dataquality/index'], resolve),
    meta: {
      title: `${i18n.$t('DataQuality')}`
    },
    redirect: {
      name: 'data-quality-result'
    },
    children: [
      {
        path: '/dataquality/result',
        name: 'data-quality-result',
        component: resolve => require(['../../pages/dataquality/pages/result/index'], resolve),
        meta: {
          title: `${i18n.$t('DataQuality Result')}`
        }
      },
      {
        path: '/dataquality/rule',
        name: 'data-quality-rule',
        component: resolve => require(['../../pages/dataquality/pages/rule/index'], resolve),
        meta: {
          title: `${i18n.$t('DataQuality Rule')}`
        }
      }
    ]
  }
]

export default dataquality

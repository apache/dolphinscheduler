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

import { useI18n } from 'vue-i18n'
import { RenderPrefix } from 'naive-ui/es/pagination/src/interface'

export default function totalCount(params: Parameters<RenderPrefix>[0]) {
  const { t } = useI18n()

  const prefix = t('project.list.total_items')
  const count = Number.prototype.toLocaleString.call(params?.itemCount ?? 0)

  return `${prefix} ${count}`
}

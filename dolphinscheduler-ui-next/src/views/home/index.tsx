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

import { defineComponent } from 'vue'
import styles from './index.module.scss'
import Card from '@/components/card'
import PieChart from '@/components/chart/modules/Pie'
import GaugeChart from '@/components/chart/modules/Gauge'
import BarChart from '@/components/chart/modules/Bar'

export default defineComponent({
  name: 'home',
  setup() {},
  render() {
    return (
      <div class={styles.container}>
        <Card title='test'>{{ default: () => <PieChart /> }}</Card>
        <Card title='test'>{{ default: () => <GaugeChart /> }}</Card>
        <Card title='test'>{{ default: () => <BarChart /> }}</Card>
      </div>
    )
  },
})

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

import { defineComponent, PropType } from 'vue'
import { useProcessDefinition } from '../use-process-definition'
import BarChart from '@/components/chart/modules/Bar'
import Card from '@/components/card'

const props = {
  title: {
    type: String as PropType<string>
  }
}

const DefinitionCard = defineComponent({
  name: 'DefinitionCard',
  props,
  setup() {
    const { getProcessDefinition } = useProcessDefinition()
    const processDefinition = getProcessDefinition()

    return { processDefinition }
  },
  render() {
    const { title, processDefinition } = this

    return (
      processDefinition.xAxisData.length > 0 &&
      processDefinition.seriesData.length > 0 && (
        <Card title={title}>
          {{
            default: () => (
              <BarChart
                xAxisData={processDefinition.xAxisData}
                seriesData={processDefinition.seriesData}
              />
            )
          }}
        </Card>
      )
    )
  }
})

export default DefinitionCard

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
import { useWorkflowDefinition } from '../use-workflow-definition'
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
    const { getWorkflowDefinition } = useWorkflowDefinition()
    const workflowDefinition = getWorkflowDefinition()

    return { workflowDefinition: workflowDefinition }
  },
  render() {
    const { title, workflowDefinition } = this

    return (
      <Card title={title}>
        {{
          default: () =>
            workflowDefinition.xAxisData.length > 0 &&
            workflowDefinition.seriesData.length > 0 && (
              <BarChart
                xAxisData={workflowDefinition.xAxisData}
                seriesData={workflowDefinition.seriesData}
              />
            )
        }}
      </Card>
    )
  }
})

export default DefinitionCard

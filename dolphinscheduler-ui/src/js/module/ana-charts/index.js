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
import echarts from 'echarts'

import Line from './packages/line'
import Bar from './packages/bar'
import Pie from './packages/pie'
import Radar from './packages/radar'
import Funnel from './packages/funnel'
import Scatter from './packages/scatter'
import { checkKeyInModel, init } from './common'

const components = {
  Line,
  Bar,
  Pie,
  Radar,
  Funnel,
  Scatter
}

const Chart = {
  // Default configuration
  settings: {},
  /**
   * Configure global properties
   * @param {Object} options Global configuration item
   */
  config (options) {
    const { theme } = options
    // Registration theme
    if (theme) {
      checkKeyInModel(theme, 'name', 'data')
      echarts.registerTheme(theme.name, theme.data)
      if (theme.default) {
        Chart.settings.defaultTheme = theme.name
      }
    }
  }
}

// Corresponding methods for injection of different components
for (const key in components) {
  if (Object.prototype.hasOwnProperty.call(components, key)) {
    Chart[key.toLowerCase()] = (el, data, options) => {
      return init(components[key], el, data, options)
    }
  }
}

export {
  Line,
  Bar,
  Pie,
  Radar,
  Funnel,
  Scatter
}

export default Chart

if (typeof window !== 'undefined') {
  window.Chart = Chart
}

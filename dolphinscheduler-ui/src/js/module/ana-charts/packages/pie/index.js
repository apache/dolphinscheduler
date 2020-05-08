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
import Base from '../base'
import { checkKeyInModel, init } from '../../common'

const TYPE = 'pie'

/**
 * Pie chart
 */
export default class Pie extends Base {
  /**
   * Initialization method called on separate export
   * @param {*} el Selector or DOM object
   * @param {*} data data source
   * @param {*} options Optional
   */
  static init (el, data, options) {
    return init(Pie, el, data, options)
  }

  /**
   * Convert user configuration to a configuration format that conforms to the format of echarts API
   */
  transform () {
    const {
      // data
      data = [],
      // title
      title = 'Pie chart',
      // Ring chart or not
      ring = false,
      // Attribute dictionary
      keyMap = {
        textKey: 'key',
        dataKey: 'value'
      }
    } = this.settings

    if (data.length === 0) {
      throw new Error('数据源为空！')
    }

    // Attribute name corresponding to text and attribute name corresponding to data value
    const { textKey, dataKey } = keyMap
    checkKeyInModel(data[0], textKey, dataKey)

    const legendData = []
    const radius = ring ? ['50%', '70%'] : '60%'
    const center = title ? ['50%', '60%'] : ['50%', '50%']
    const series = [{
      radius: radius,
      center: center,
      type: TYPE,
      data: []
    }]

    // Fill data
    for (let i = 0; i < data.length; i++) {
      const element = data[i]
      const { [dataKey]: value, [textKey]: name, ...other } = element
      const item = {
        value,
        name,
        ...other,
        _raw: element
      }
      series[0].data.push(item)
    }
    return { title, series, legendData }
  }

  /**
   * Drawing charts
   */
  apply () {
    const { title, series, legendData } = this.options

    // Injection configuration to series
    const { insertSeries } = this.settings
    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    const opts = {
      title: {
        text: title,
        x: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: legendData
      },
      series: _series
    }

    this.echart.setOption(opts, true)
    this.echart.clear()
    this.echart.setOption(opts, true)
  }
}

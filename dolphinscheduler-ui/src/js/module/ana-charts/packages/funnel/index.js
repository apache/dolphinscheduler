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

const TYPE = 'funnel'

/**
 * Funnel plot
 */
export default class Funnel extends Base {
  /**
   * Initialization method called on separate export
   * @param {*} el Selector or DOM pair
   * @param {*} data data source
   * @param {*} options Optional
   */
  static init (el, data, options) {
    return init(Funnel, el, data, options)
  }

  /**
   * Convert user configuration to a configuration format that conforms to the format of echarts API
   */
  transform () {
    const {
      // data
      data = [],
      // title
      title = 'Funnel plot',
      // Attribute dictionary
      keyMap = {
        textKey: 'key',
        dataKey: 'value'
      }
    } = this.settings

    if (data.length === 0) {
      throw new Error('Data source is empty!')
    }

    // Attribute name corresponding to text and attribute name corresponding to data value
    const { textKey, dataKey } = keyMap
    checkKeyInModel(data[0], textKey, dataKey)

    const legendData = []
    const series = [{
      type: TYPE,
      left: '10%',
      top: 60,
      bottom: 60,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: {
        normal: {
          show: true,
          position: 'inside'
        },
        emphasis: {
          textStyle: {
            fontSize: 20
          }
        }
      },
      labelLine: {
        normal: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        }
      },
      itemStyle: {
        normal: {
          borderColor: '#fff',
          borderWidth: 1
        }
      },
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

    this.echart.setOption({
      title: {
        text: title
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c}%'
      },
      legend: {
        data: legendData
      },
      series: _series
    }, true)
  }
}

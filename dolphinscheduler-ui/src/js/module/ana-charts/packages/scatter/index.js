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

const TYPE = 'scatter'

/**
 * Bubble chart
 */
export default class Scatter extends Base {
  /**
   * Initialization method called on separate export
   * @param {*} el Selector or DOM object
   * @param {*} data data source
   * @param {*} options Optional
   */
  static init (el, data, options) {
    return init(Scatter, el, data, options)
  }

  /**
   * Convert user configuration to a configuration format that conforms to the format of echarts API
   */
  transform () {
    const {
      // data
      data = [],
      // Chart title
      title = 'Bubble chart',
      // Attribute dictionary
      keyMap = {
        xKey: 'x',
        yKey: 'y',
        sizeKey: 'size',
        textKey: 'text',
        legendKey: 'typeName'
      }
    } = this.settings

    if (data.length === 0) {
      throw new Error('Data source is empty!')
    }

    const legendData = []
    const series = []

    const { xKey, yKey, sizeKey, textKey, legendKey } = keyMap
    checkKeyInModel(data[0], xKey, yKey, sizeKey, textKey, legendKey)

    for (let i = 0; i < data.length; i++) {
      const {
        [legendKey]: legendItem,
        [xKey]: xValue,
        [yKey]: yValue,
        [sizeKey]: sizeValue,
        [textKey]: textValue,
        ...other
      } = data[i]

      // Legend
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // series
      let targetSeries = series.find(s => s.name === legendItem)
      if (!targetSeries) {
        targetSeries = {
          type: TYPE,
          name: legendItem,
          data: [],
          symbolSize: function (data) {
            return Math.sqrt(data[2])
          },
          label: {
            emphasis: {
              show: true,
              formatter: function (param) {
                return param.data[3]
              },
              position: 'top'
            }
          }
        }
        series.push(targetSeries)
      }
      targetSeries.data.push({
        value: [
          xValue,
          yValue,
          sizeValue,
          textValue
        ],
        ...other,
        _raw: data[i]
      })
    }

    return { title, series, legendData }
  }

  /**
   * Drawing charts
   */
  apply () {
    const { title, series, legendData = [] } = this.options

    const {
      // Custom X axis
      xAxis,
      // Custom Y axis
      yAxis,
      // Injection configuration to series
      insertSeries
    } = this.settings
    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    this.echart.setOption({
      title: {
        text: title
      },
      legend: {
        right: 10,
        data: legendData
      },
      xAxis: xAxis || {},
      yAxis: yAxis || {},
      series: _series
    }, true)
  }
}

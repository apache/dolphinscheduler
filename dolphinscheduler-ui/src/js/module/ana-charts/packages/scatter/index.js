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
 * 气泡图
 */
export default class Scatter extends Base {
  /**
   * 单独导出时调用的初始化方法
   * @param {*} el 选择器或者 DOM 对象
   * @param {*} data 数据源
   * @param {*} options 可选项
   */
  static init (el, data, options) {
    return init(Scatter, el, data, options)
  }

  /**
   * 将用户配置转换为符合 ECharts API 格式的配置格式
   */
  transform () {
    const {
      // 数据
      data = [],
      // 图表标题
      title = '气泡图',
      // 属性字典
      keyMap = {
        xKey: 'x',
        yKey: 'y',
        sizeKey: 'size',
        textKey: 'text',
        legendKey: 'typeName'
      }
    } = this.settings

    if (data.length === 0) {
      throw new Error('数据源为空！')
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

      // 图例
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // 系列
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
   * 绘制图表
   */
  apply () {
    const { title, series, legendData = [] } = this.options

    let {
      // 自定义 x 轴
      xAxis,
      // 自定义 y 轴
      yAxis,
      // 注入配置到 series
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

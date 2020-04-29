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
export default function (value) {
  return {
    series: [
      {
        type: 'gauge',
        center: ['50%', '45%'], // Instrument position
        radius: '80%', // Instrument size
        startAngle: 200, // Starting angle
        endAngle: -20, // End angle
        axisLine: {
          show: false,
          lineStyle: { // Property linestyle controls line style
            color: [
              [ 0.5, new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ // eslint-disable-line
                offset: 1,
                color: '#1CAD52'// 50% Color in place
              }, {
                offset: 0.8,
                color: '#1CAD52'// 40% Color in place
              }], false)], // 100% Color in place
              [ 0.7, new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ // eslint-disable-line
                offset: 1,
                color: '#FFC539'// 70% Color in place
              }, {
                offset: 0.8,
                color: '#FEEC49'// 66% Color in place
              }, {
                offset: 0,
                color: '#C7DD6B'// 50% Color in place
              }], false)],
              [ 0.9, new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ // eslint-disable-line
                offset: 1,
                color: '#E75F25' // 90% Color in place
              }, {
                offset: 0.8,
                color: '#FE951E' // 86% Color in place
              }, {
                offset: 0,
                color: '#FFC539' // 70% Color in place
              }], false)],
              [1, new echarts.graphic.LinearGradient(0, 0, 0, 1, [ { // eslint-disable-line
                offset: 0.2,
                color: '#E75F25' // 92% Color in place
              }, {
                offset: 0,
                color: '#D9452C' // 90% Color in place
              }], false)]
            ],
            width: 10
          }
        },
        splitLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        axisLabel: {
          show: false
        },
        pointer: { // Pointer style
          length: '45%'
        },
        detail: {
          show: false
        }
      },
      {
        type: 'gauge',
        center: ['50%', '45%'], // Default global center
        radius: '70%',
        startAngle: 200,
        endAngle: -20,
        axisLine: {
          show: true,
          lineStyle: { // Property linestyle controls line style
            color: [ // Dial Color
              [0.5, '#20AE51'], // 0-50%Color in place
              [0.7, '#FFED44'], // 51%-70%Color in place
              [0.9, '#FF9618'], // 70%-90%Color in place
              [1, '#DA462C']// 90%-100%Color in place
            ],
            width: 30// Dial width
          }
        },
        splitLine: { // Split line style (and 10, 20 equal length line style)
          length: 30,
          lineStyle: { // Property linestyle controls line style
            width: 2
          }
        },
        axisTick: { // Tick mark style (and short line style)
          length: 20
        },
        axisLabel: { // Text style (and text styles such as "10", "20")
          color: 'black',
          distance: 5 // Distance between text and dial
        },
        detail: {
          formatter: '{score|{value}%}',
          offsetCenter: [0, '50%'],
          backgroundColor: '#2D8BF0',
          height: 30,
          rich: {
            score: {
              color: 'white',
              fontFamily: 'Microsoft YaHei',
              fontSize: 32
            }
          }
        },
        data: [{
          value: value || 0,
          label: {
            textStyle: {
              fontSize: 12
            }
          }
        }]
      }
    ]
  }
}

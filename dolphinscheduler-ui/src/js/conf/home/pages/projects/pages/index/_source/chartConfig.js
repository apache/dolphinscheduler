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

import _ from 'lodash'
import { tasksState } from '@/conf/home/pages/dag/_source/config'

const pie = {
  series: [
    {
      type: 'pie',
      clickable: true, // Whether to open clicks
      minAngle: 5, // The smallest sector angle (0 ~ 360), used to prevent a value from being too small, causing the sector to be too small to affect the interaction
      avoidLabelOverlap: true, // Whether to prevent the label overlap policy
      hoverAnimation: true, // Whether to enable hover to enlarge the animation on the sector.
      radius: ['30%', '60%'],
      center: ['53%', '60%'],
      label: {
        align: 'left',
        normal: {
        }
      }
    }
  ]
}

const bar = {
  title: {
    text: ''
  },
  grid: {
    right: '2%'
  },
  xAxis: {
    splitLine: {
      show: false
    },
    axisLabel: {
      formatter (v) {
        return `${v.split(',')[0]} (${v.split(',')[2]})`
      }
    }
  },
  tooltip: {
    formatter (v) {
      const val = v[0].name.split(',')
      return `${val[0]} (${v[0].value})`
    }
  },
  series: [{
    type: 'bar',
    barWidth: 30
  }]
}

const simple = {
  xAxis: {
    splitLine: {
      show: false
    },
    axisLabel: {
      interval: 0,
      showMaxLabel: true,
      formatter (v) {
        return tasksState[v].desc
      }
    }
  },
  tooltip: {
    formatter (data) {
      let str = ''
      _.map(data, (v, i) => {
        if (i === 0) {
          str += `${tasksState[v.name].desc}<br>`
        }
        str += `<div style="font-size: 12px;">${v.seriesName} : ${v.data}<br></div>`
      })
      return str
    }
  },
  color: ['#D5050B', '#0398E1']

}

export { pie, bar, simple }

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

import { isNumber, sumBy } from 'lodash'
import type {
  TableColumns,
  CommonColumnInfo
} from 'naive-ui/es/data-table/src/interface'

export const COLUMN_WIDTH_CONFIG = {
  selection: {
    width: 50
  },
  index: {
    width: 50
  },
  linkName: {
    width: 200
  },
  linkEllipsis: {
    style: 'max-width: 180px;line-height: 1.5'
  },
  name: {
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  state: {
    width: 120
  },
  type: {
    width: 130
  },
  version: {
    width: 80
  },
  time: {
    width: 180
  },
  timeZone: {
    width: 220
  },
  operation: (number: number): CommonColumnInfo => ({
    fixed: 'right',
    width: Math.max(30 * number + 12 * (number - 1) + 24, 100)
  }),
  userName: {
    width: 120,
    ellipsis: {
      tooltip: true
    }
  },
  ruleType: {
    width: 120
  },
  note: {
    width: 180,
    ellipsis: {
      tooltip: true
    }
  },
  dryRun: {
    width: 140
  },
  times: {
    width: 120
  },
  duration: {
    width: 120
  },
  yesOrNo: {
    width: 100,
    ellipsis: {
      tooltip: true
    }
  },
  size: {
    width: 100
  },
  tag: {
    width: 160
  },
  copy: {
    width: 50
  }
}

export const calculateTableWidth = (columns: TableColumns) =>
  sumBy(columns, (column) => (isNumber(column.width) ? column.width : 0))

export const DefaultTableWidth = 1800

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
export const X6_NODE_NAME = 'dag-task'
export const X6_EDGE_NAME = 'dag-edge'
export const X6_PORT_OUT_NAME = 'dag-port-out'

const EDGE = '#999999'
const BG_BLUE = '#DFE9F7'
const BG_WHITE = '#FFFFFF'
const NODE_BORDER = '#CCCCCC'
const TITLE = '#333'
const STROKE_BLUE = '#288FFF'

export const PORT_PROPS = {
  groups: {
    [X6_PORT_OUT_NAME]: {
      position: {
        name: 'absolute',
        args: {
          x: 200,
          y: 24
        }
      },
      markup: [
        {
          tagName: 'g',
          selector: 'body',
          children: [
            {
              tagName: 'circle',
              selector: 'circle-outer'
            },
            {
              tagName: 'text',
              selector: 'plus-text'
            },
            {
              tagName: 'circle',
              selector: 'circle-inner'
            }
          ]
        }
      ],
      attrs: {
        body: {
          magnet: true
        },
        'plus-text': {
          fontSize: 12,
          fill: NODE_BORDER,
          text: '+',
          textAnchor: 'middle',
          x: 0,
          y: 3
        },
        'circle-outer': {
          stroke: NODE_BORDER,
          strokeWidth: 1,
          r: 6,
          fill: BG_WHITE
        },
        'circle-inner': {
          r: 4,
          fill: 'transparent'
        }

      }
    }
  }
}

export const PORT_HIGHLIGHT_PROPS = {
  [X6_PORT_OUT_NAME]: {
    attrs: {
      'circle-outer': {
        stroke: STROKE_BLUE,
        fill: BG_BLUE,
        r: 8
      },
      'circle-inner': {
        fill: STROKE_BLUE,
        r: 6
      }
    }
  }
}

export const NODE_STATUS_MARKUP = [{
  tagName: 'foreignObject',
  selector: 'fo',
  children: [
    {
      tagName: 'body',
      selector: 'fo-body',
      ns: 'http://www.w3.org/1999/xhtml',
      children: [{
        tagName: 'div',
        selector: 'status'
      }]
    }
  ]
}]

export const NODE_PROPS = {
  width: 220,
  height: 48,
  markup: [
    {
      tagName: 'rect',
      selector: 'body',
      className: 'dag-task-body'
    },
    {
      tagName: 'image',
      selector: 'image'
    },
    {
      tagName: 'text',
      selector: 'title'
    }
  ],
  attrs: {
    body: {
      refWidth: '100%',
      refHeight: '100%',
      rx: 6,
      ry: 6,
      pointerEvents: 'visiblePainted',
      fill: BG_WHITE,
      stroke: NODE_BORDER,
      strokeWidth: 1,
      strokeDasharray: 'none'
    },
    image: {
      width: 30,
      height: 30,
      refX: 12,
      refY: 9
    },
    title: {
      refX: 45,
      refY: 18,
      fontFamily: 'Microsoft Yahei',
      fontSize: 12,
      fontWeight: 'bold',
      fill: TITLE,
      strokeWidth: 0
    },
    fo: {
      refX: '46%',
      refY: -25,
      width: 18,
      height: 18
    }
  },
  ports: {
    ...PORT_PROPS,
    items: [
      {
        id: X6_PORT_OUT_NAME,
        group: X6_PORT_OUT_NAME
      }
    ]
  }
}

export const NODE_HIGHLIGHT_PROPS = {
  attrs: {
    body: {
      fill: BG_BLUE,
      stroke: STROKE_BLUE,
      strokeDasharray: '5,2'
    },
    title: {
      fill: STROKE_BLUE
    }
  }
}

export const EDGE_PROPS = {
  attrs: {
    line: {
      stroke: EDGE,
      strokeWidth: 1,
      targetMarker: {
        tagName: 'path',
        fill: EDGE,
        strokeWidth: 0,
        d: 'M 6 -3 0 0 6 3 Z'
      }
    }
  },
  connector: {
    name: 'rounded'
  },
  router: {
    name: 'manhattan',
    args: {
      endDirections: ['top', 'bottom', 'left']
    }
  },
  defaultLabel: {
    markup: [
      {
        tagName: 'rect',
        selector: 'body'
      },
      {
        tagName: 'text',
        selector: 'label'
      }
    ],
    attrs: {
      label: {
        fill: EDGE,
        fontSize: 14,
        textAnchor: 'middle',
        textVerticalAnchor: 'middle',
        pointerEvents: 'none'
      },
      body: {
        ref: 'label',
        fill: BG_WHITE,
        stroke: EDGE,
        strokeWidth: 1,
        rx: 4,
        ry: 4,
        refWidth: '140%',
        refHeight: '140%',
        refX: '-20%',
        refY: '-20%'
      }
    },
    position: {
      distance: 0.5,
      options: {
        absoluteDistance: true,
        reverseDistance: true
      }
    }
  }
}

export const EDGE_HIGHLIGHT_PROPS = {
  attrs: {
    line: {
      stroke: STROKE_BLUE,
      targetMarker: {
        fill: STROKE_BLUE
      }
    }
  },
  defaultLabel: {
    attrs: {
      label: {
        fill: STROKE_BLUE
      },
      body: {
        fill: BG_WHITE,
        stroke: STROKE_BLUE
      }
    }
  }
}

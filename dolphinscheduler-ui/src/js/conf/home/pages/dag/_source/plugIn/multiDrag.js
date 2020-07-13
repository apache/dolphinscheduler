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
import JSP from './jsPlumbHandle'
/**
 * when and only ctrl or meta key pressing, we can select one or more dags to drag
 */
export default function () {
  // init
  let selectableObjects = []
  JSP.JspInstance.clearDragSelection()
  let ctrlPress = false

  let nodes = null
  const $window = $(window)

  $window.bind('keydown', function (event) {
    if (event.ctrlKey || event.metaKey) {
      if (nodes) {
        nodes.unbind('mousedown', select)
      }
      nodes = $('.jtk-draggable')
      nodes.bind('mousedown', select)
      ctrlPress = true
    }
  })
  $window.bind('keyup', function (event) {
    clear()
  })

  function select (event) {
    if (ctrlPress && event.button === 0) {
      let index = null
      if ((index = selectableObjects.indexOf(this)) !== -1) {
        selectableObjects.splice(index, 1)
        JSP.JspInstance.removeFromDragSelection(this)
        $(this).css('border-color', '')
      } else {
        selectableObjects.push(this)
        JSP.JspInstance.addToDragSelection(this)
        $(this).css('border-color', '#4af')
      }
    }
  }

  function clear () {
    ctrlPress = false
    selectableObjects.map(item => {
      $(item).css('border-color', '')
    })
    selectableObjects = []
    JSP.JspInstance.clearDragSelection()
  }
}

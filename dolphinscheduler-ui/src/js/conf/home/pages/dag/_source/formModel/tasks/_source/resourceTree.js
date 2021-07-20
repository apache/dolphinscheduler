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
export function diGuiTree (items) { // Recursive convenience tree structure
  items.forEach(item => {
    item.children === '' || item.children === undefined || item.children === null || item.children.length === 0
      ? operationTree(item) : diGuiTree(item.children)
  })
}

export function operationTree (item) {
  if (item.dirctory) {
    item.isDisabled = true
  }
  delete item.children
}

export function searchTree (element, id) {
  // 根据id查找节点
  if (element.id === id) {
    return element
  } else if (element.children) {
    let i
    let result = null
    for (i = 0; result === null && i < element.children.length; i++) {
      result = searchTree(element.children[i], id)
    }
    return result
  }
  return null
}

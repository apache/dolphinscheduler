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

const removeUselessChildren = (
  list: {
    children?: []
    directory?: boolean
    disabled?: boolean
    dirctory?: boolean
  }[]
) => {
  if (!list.length) return
  list.forEach((item) => {
    if (item.dirctory && item.children?.length === 0) item.disabled = true
    if (!item.children) return
    if (item.children.length === 0) {
      delete item.children
      return
    }
    removeUselessChildren(item.children)
  })
}

export default removeUselessChildren

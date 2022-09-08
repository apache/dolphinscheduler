.. Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

..   http://www.apache.org/licenses/LICENSE-2.0

.. Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.

Local
=====

`Local` is a local resource plugin for pydolphinscheduler.

When using a local resource plugin, you only need to add the `resource_plugin` parameter in the task subclass or workflow definition,
such as `resource_plugin=Local("/tmp")`.


For the specific use of resource plugins, you can see `How to use` in :doc:`./resource-plugin`

Dive Into
---------

.. automodule:: pydolphinscheduler.resources_plugin.local
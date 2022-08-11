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

How to develop
==============

You need your plugin class created under dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/resources_plugin folder.

Your plugin class needs two indispensable methods, one is the __init__ method, the parameter is the prefix of type str, the other is
the read_file function, the parameter is the file suffix of type str, the return value is the file content, if it is exists and is readable.

In addition you need to add a constant with your plugin name to ResourcePluginType in dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/constants.py, eg LOCAL = "local".

Example
-------
.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/local.py
   :start-after: [start local_res_definition]
   :end-before: [end local_res_definition]

.. literalinclude:: ../../../src/pydolphinscheduler/constants.py
   :start-after: [start res_plugin_constants_definition]
   :end-before: [end res_plugin_constants_definition]


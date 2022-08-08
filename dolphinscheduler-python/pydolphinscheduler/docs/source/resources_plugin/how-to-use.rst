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

How to use
==========
This article will show you how to use resource plug-ins.

When you initialize the task subclass, you can add the parameter resource_plugin parameter, specify the type and
prefix of the resource plugin, the command at this time should be the file path, in other words, the prefix of the
resource plugin plus the command at this time is the command file in the specified Absolute paths in resource plugins.

When the resource_plugin parameter is defined in both the task subclass and the workflow, the resource_plugin defined in the task subclass is used first. If the task subclass does not define resource_plugin, but the resource_plugin is defined in the workflow, the resource_plugin in the workflow is used.

Of course, if neither the task subclass nor the workflow specifies resource_plugin, the command at this time will be executed as a script, in other words, we are forward compatible.


Example
-------
.. literalinclude:: ../../../src/pydolphinscheduler/examples/tutorial.py
   :start-after: [start workflow_declare]
   :end-before: [end task_relation_declare]

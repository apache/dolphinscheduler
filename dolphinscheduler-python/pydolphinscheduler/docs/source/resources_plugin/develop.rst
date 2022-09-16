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

When you want to create a new resource plugin, you need to add a new class in the module `resources_plugin`.

The resource plugin class needs to inherit the abstract class `ResourcePlugin` and implement its abstract method `read_file` function.

The parameter of the `__init__` function of `ResourcePlugin` is the prefix of STR type. You can override this function when necessary.

The `read_file` function parameter of `ResourcePlugin` is the file suffix of STR type, and its return value is the file content, if it exists and is readable.


Example
-------
- Method `__init__`: Initiation method with `param`:`prefix`

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/local.py
    :start-after: [start init_method]
    :end-before: [end init_method]

- Method `read_file`: Get content from the given URI, The function parameter is the suffix of the file path.

The file prefix has been initialized in init of the resource plugin.

The prefix plus suffix is the absolute path of the file in this resource.

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/local.py
    :start-after: [start read_file_method]
    :end-before: [end read_file_method]

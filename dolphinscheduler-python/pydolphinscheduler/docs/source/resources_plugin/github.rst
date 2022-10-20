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

GitHub
======

`GitHub` is a github resource plugin for pydolphinscheduler.

When using a github resource plugin, you only need to add the `resource_plugin` parameter in the task subclass or workflow definition,
such as `resource_plugin=GitHub(prefix="https://github.com/xxx", access_token="ghpxx")`.
The token parameter is optional. You need to add it when your repository is a private repository.

You can view this `document <https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token>`_
when creating a token.

For the specific use of resource plugins, you can see `How to use` in :doc:`resource-plugin`

Dive Into
---------

.. automodule:: pydolphinscheduler.resources_plugin.github
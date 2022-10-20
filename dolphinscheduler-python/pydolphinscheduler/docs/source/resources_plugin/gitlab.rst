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

GitLab
======

`GitLab` is a gitlab resource plugin for pydolphinscheduler.

When using a gitlab resource plugin, you only need to add the `resource_plugin` parameter in the task subclass or workflow definition,
such as `resource_plugin=GitLab(prefix="xxx")`, if it is a public repository.

If it is a private or Internal repository, you can use three ways to obtain authentication.

The first is `Personal Access Tokens`, using `resource_plugin=GitLab(prefix="xxx", private_token="xxx")`.

The second method is to obtain authentication through `username` and `password`:

using `resource_plugin=GitLab(prefix="xxx", username="username", password="pwd")`.

The third method is to obtain authentication through `OAuth Token`:

using `resource_plugin=GitLab(prefix="xxx", oauth_token="xx")`.

You can view this `document <https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html#create-a-personal-access-token>`_
when creating a `Personal Access Tokens`.

For the specific use of resource plugins, you can see `How to use` in :doc:`resource-plugin`

Dive Into
---------

.. automodule:: pydolphinscheduler.resources_plugin.gitlab
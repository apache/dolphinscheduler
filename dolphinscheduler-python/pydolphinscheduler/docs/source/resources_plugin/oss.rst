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

OSS
===

`OSS` is a Aliyun OSS resource plugin for pydolphinscheduler.

When using a OSS resource plugin, you only need to add the `resource_plugin` parameter in the task subclass or workflow definition,
such as `resource_plugin=OSS(prefix="xxx")`, if the file is publicly readable.

When the file is private, using `resource_plugin=OSS(prefix="xxx", access_key_id="xxx", access_key_secret="xxx")`

Notice
The read permission of files in a bucket is inherited from the bucket by default. In other words, if the bucket is private,
the files in it are also private.

But the read permission of the files in the bucket can be changed, in other words, the files in the private bucket can also be read publicly.

So whether the `AccessKey` is needed depends on whether the file is private or not.

You can view this `document <https://www.alibabacloud.com/help/en/tablestore/latest/how-can-i-obtain-an-accesskey-pair>`_
when creating a pair `AccessKey`.

For the specific use of resource plugins, you can see `How to use` in :doc:`resource-plugin`

Dive Into
---------

.. automodule:: pydolphinscheduler.resources_plugin.OSS

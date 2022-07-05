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

Submit Your Code from Different machine
=======================================

Generally, we use pydolphinscheduler as a client to DolphinScheduler, and consider we may change our workflow
code frequently, the best practice is running :ref:`python gateway service <start:start python gateway service>`
in your server machine and submit the workflow code from your development machine, like a laptop or PC. This behavior
is supported by pydolphinscheduler out of box with one or two single command lines. 

Export Configuration File
-------------------------

.. code-block:: bash

   pydolphinscheduler config --init

your could find more detail in :ref:`configuration exporting <config:export configuration file>`

Run API Server in Other Host
----------------------------

.. code-block:: bash

   pydolphinscheduler config --set java_gateway.address <your-api-server-ip-or-hostname>

your could find more detail in :ref:`configuration setting <config:change configuration>`

Run API Server in Other Port
----------------------------

.. code-block:: bash

   pydolphinscheduler config --set java_gateway.port <your-python-gateway-service-port>

your could find more detail in :ref:`configuration setting <config:change configuration>`

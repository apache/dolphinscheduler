# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""
A example for create or update resource.

This example creates or modifies a resource whose full name is `/test.py`.
When the process definition is submitted, this resource file is also submitted along with it.
"""

from pydolphinscheduler.core import ProcessDefinition
from pydolphinscheduler.core.resource import Resource
from pydolphinscheduler.tasks import Shell

resource = Resource(
    name="/test.py", content="""print("hello world")""", description="Test file."
)
with ProcessDefinition(
    name="resource_example", tenant="tenant_exists", resource_list=[resource]
) as pd:
    test_task = Shell(name="test_task", command="echo 1")
    pd.submit()
